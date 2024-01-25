from flask import Flask,request,redirect,url_for,json
from flask import jsonify
import mysql.connector
import base64
import os
from uuid import uuid4
from passlib.hash import sha256_crypt
from datetime import datetime,date
from flask.json.provider import DefaultJSONProvider
from flask import current_app

class UpdatedJSONProvider(DefaultJSONProvider):
    def default(self, o):
        if isinstance(o, datetime):
            return o.strftime('%d-%m-%Y %H:%M:%S')
        elif isinstance(o, date):
            return o.strftime('%d-%m-%Y')
        return super().default(o)

app = Flask(__name__)
app.json.ensure_ascii = False
app.json = UpdatedJSONProvider(app)
json.provider.DefaultJSONProvider.ensure_ascii = False



@app.route('/', methods=['GET', 'POST'])
def welcome():
    return "Hello World!!!"


@app.route('/login',methods=['POST'])
def login():
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	args = request.args
	username=args.get("username")
	password=args.get("password")
	cursor=cnx.cursor()
	cursor.execute("select UserId,Username,Password from Users where Username='{}'".format(username))
	conta=cursor.fetchone()
	
	if conta == None:
		cursor.close()
		cnx.close()
		return "",403
	else:
		
		hash_pass=conta[2]
	if 	sha256_crypt.verify(password,hash_pass):
		newToken=uuid4()		
		cursor.execute("update Users set token = '{}' where Username='{}'".format(newToken,username))
		cnx.commit()
		cursor.close()
		cnx.close()
		return jsonify({'token': newToken})
	else:
		cursor.close()
		cnx.close()
		return "",403


@app.route('/register',methods=['POST'])
def register():
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	args = request.args
	username=args.get("username")
	createdDate=datetime.now()		
	password=sha256_crypt.hash(args.get("password"))
	cursor=cnx.cursor()
	cursor.execute("select Username from Users where Username='{}'".format(username))
	conta=cursor.fetchone()
	if conta != None:
		cursor.close()
		cnx.close()
		return jsonify(Code="409",Description="Account already exists")
	else:
		if args.get("secretAdmin") == "PasswordSecreta":
			admin=True
		else:
			admin=False		
		cursor.execute("insert into Users (Admin,CreatedDate,username, password) values ({},'{}','{}','{}')".format(admin,createdDate,username,password))
		cnx.commit()
		cursor.close()
		cnx.close()
		return jsonify(Code="200",Description="Conta criada com sucesso")

@app.route('/users',methods=['GET'])
@app.route('/users/<userId>',methods=['GET','PUT'])
def getUsers(userId=None):
	args = request.args
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2',charset="utf8")
	cursor=cnx.cursor(dictionary=True)
	if(userId is None):
		cursor.execute("select UserId,Username,Name,LastName,BirthDate,CreatedDate,Weight,Height,Address,PhotoId from Users")
		conta=cursor.fetchall()
		cursor.close()
		cnx.close()
		return jsonify(conta)
	else:
		if(request.method=="GET"):
			#cursor.execute("select UserId,Username,Name,LastName,BirthDate,CreatedDate,Weight,Height,Address,PhotoId from Users where Username = '{}'".format(userId))
			cursor.execute("select u.UserId,u.Username,u.Name,u.LastName,u.BirthDate,u.CreatedDate,u.Weight,u.Height,u.Address,u.PhotoId,p.PathToPhoto PhotoPath from Users u left join Photos p on p.PhotoId=u.PhotoId where u.Username = '{}'".format(userId))
			conta=cursor.fetchone()
			cursor.close()
			cnx.close()
			return jsonify(conta)
		if(request.method=="PUT"):			
			token = request.headers.get("auth")
			cursor.execute("select Token from Users where Username = '{}'".format(userId))
			token2=cursor.fetchone()["Token"]	
			print(token)		
			if(token == token2):
				print(request.json)
				for value in request.json:
					print(value)
					if(request.json[value]==None or request.json[value]=="None"):
						request.json[value]="NULL"
				name=request.json["Name"]
				lastName=request.json["LastName"]
				weight=request.json["Weight"]
				birthDate=request.json["BirthDate"]
				address=request.json["Address"]
				height=request.json["Height"]

				str=""
				for i in request.args:
					str+="{} = '{}'".format(i.capitalize(),request.args.get(i))
					if(i != list(request.args)[-1]):
						str+=","
				cursor.execute("update Users set Name='{}',LastName='{}',Weight={},Address='{}',Height={} where Username = '{}'".format(name,lastName,weight,address,height,userId))
				if(request.json["BirthDate"]!="NULL"):
					cursor.execute("update Users set BirthDate=STR_TO_DATE('{}', '%d-%m-%Y %H:%i') where Username = '{}'".format(birthDate,userId))
				cnx.commit()
				cursor.close()
				cnx.close()
				return jsonify(Code="200",Description="Conta atualizada com sucesso")

			else:
				print(request.args)
				cursor.close()
				cnx.close()
				return jsonify(Code="404",Description="Erro ao atualizar conta!")

			
@app.route('/users/<userId>/groups',methods=['GET'])
def getUserGroups(userId=None):	
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2',charset="utf8")
	cursor=cnx.cursor(dictionary=True)
	cursor.execute("select Name,g.GroupId as GroupId,g.CreatedDate as CreatedDate,g.City as City,g.OwnerId as OwnerId, p.PathToPhoto as PhotoPath from RunGroups g inner join  GroupMembers gm on g.GroupId=gm.GroupId left join Photos p on p.PhotoId=g.PhotoId where gm.userId={}".format(userId))
	groups=cursor.fetchall()
	cursor.close()
	cnx.close()
	return jsonify(groups)

@app.route('/users/<userId>/posts',methods=['GET'])
def getUserPosts(userId=None):	
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2',charset="utf8")
	cursor=cnx.cursor(dictionary=True)
	cursor.execute("select po.PostId, po.Text, po.CreatedDate, po.UserId, po.GroupId, po.PhotoId, ph.PathToPhoto PostPhotoPath, g.Name, u.Username, f.PathToPhoto UserPhotoPath from Posts po left join Photos ph on ph.PhotoId=po.PhotoId left join RunGroups g on g.GroupId=po.GroupId left join Users u on u.UserId=po.UserId left join Photos f on f.PhotoId=u.PhotoId where po.UserId='{}' order by PostId desc".format(userId))
	groups=cursor.fetchall()
	cursor.close()
	cnx.close()
	return jsonify(groups)

@app.route('/database', methods=['GET'])
def getDB():
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2',charset="utf8")
	cursor=cnx.cursor()
	cursor.execute("show tables")
	reply=cursor.fetchall()
	cursor.close()
	print(reply)
	tables=[]
	for i in list(reply):
		tables.append(str(i).split("'")[1])
	describe=[]
	cursor=cnx.cursor(dictionary=True)
	for t in tables:
		
		
		cursor.execute("describe  {};".format(t))
		reply=cursor.fetchall()
		describe.append(reply)
	

	cursor.close()
	cnx.close()
	star=""
	
	print()
	return jsonify(list(map(str,describe)))


@app.route('/groups', methods=['GET','POST'])
@app.route('/groups/<groupId>', methods=['GET','PUT','DELETE'])
def getGroups(groupId=None):
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2',charset="utf8")
	cursor=cnx.cursor(dictionary=True)
	if(groupId is None):
		if request.method=='GET':		
			cursor.execute("select g.City,g.CreatedDate,g.GroupId,g.Name,g.OwnerId,p.PathToPhoto as PhotoPath from RunGroups g left join Photos p on g.PhotoId=p.PhotoId")
			group=cursor.fetchall()
			cursor.close()
			cnx.close()
			return jsonify(group)

		if request.method=='POST':
			args = request.args
			name = args.get("name")
			city=args.get("city")
			createdDate=datetime.now()
			print(createdDate)
			cursor.execute("select Name from RunGroups where LOWER(REPLACE(Name,' ','')) = LOWER(REPLACE('{}',' ',''))".format(name))
			groupName = cursor.fetchone()
			
			if groupName is not None:
				cursor.close()
				cnx.close()
				return jsonify(Code="403",Description="Group name exists!")		
			token = request.headers.get("auth")
			cursor.execute("select UserId from Users where Token = '{}'".format(token))
			userId = cursor.fetchone()
			if userId is None:
				cursor.close()
				cnx.close()
				return "token_not_found", 404
			else:
				ownerId=userId['UserId']
				print(city)
				cursor.execute("insert into RunGroups (Name,City,OwnerId,CreatedDate) values ('{}','{}','{}','{}')".format(name,city,ownerId,createdDate))			
				cnx.commit()
				cursor.execute("select GroupId from RunGroups where Name like '{}'".format(name))
				groupId = cursor.fetchone()['GroupId']
				cursor.execute("insert into GroupMembers (GroupId,UserId,GroupAdmin) values ('{}','{}','{}')".format(groupId,ownerId,int(True)))
				cnx.commit()
				cursor.close()
				cnx.close()
				return jsonify(Code="200",Description="Group created successfully!")
		cursor.close()
		cnx.close()		
		return "",404
	else:
		if(request.method=="GET"):
			cursor.execute("select * from RunGroups where GroupId ='{}'".format(groupId))
			group=cursor.fetchone()
			cursor.close()
			cnx.close()
			return jsonify(group)

		if(request.method=="PUT"):
			token = request.headers.get("auth")
			cursor.execute("select Token from Users u inner join RunGroups g on u.UserId=g.OwnerId where g.groupId = '{}'".format(groupId))
			token2=cursor.fetchone()['Token']			
			if(token == token2):
				str=""
				for i in request.args:
					str+="{} = '{}'".format(i.capitalize(),request.args.get(i))
					if(i != list(request.args)[-1]):
						str+=","
				cursor.execute("update RunGroups set {} where groupId = '{}'".format(str,groupId))
				cnx.commit()
				cursor.close()
				cnx.close()
				return "",202
			else:
				print(request.args)
				cursor.close()
				cnx.close()
				return "",404

		if(request.method=="DELETE"):
			token = request.headers.get("auth")
			cursor.execute("select OwnerId from RunGroups where GroupId = '{}'".format(groupId))
			owner=cursor.fetchone()['OwnerId']
			cursor.execute("select Token from Users where UserId = '{}'".format(owner))
			token2=cursor.fetchone()['Token']			
			if(token == token2):
				cursor.execute("delete from RunGroups where GroupId ='{}'".format(groupId))
				cnx.commit()
				cursor.close()
				cnx.close()
				return "group_deleted",200	
			else:
				return "No_Permission",403
		else:
			cursor.close()
			cnx.close()
			return "",404

@app.route('/groups/<groupId>/members', methods=['GET','POST','DELETE'])
def getGroupMembers(groupId=None):
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	cursor=cnx.cursor(dictionary=True)	
	if request.method=='GET':	
		cursor.execute("select u.Username from GroupMembers g inner join Users u on g.UserId = u.UserId where g.GroupId='{}'".format(groupId))
		groupMembersId=cursor.fetchall()
		cursor.close()
		cnx.close()
		return jsonify(groupMembersId)			
	if request.method=='POST':
		token = request.headers.get("auth")
		cursor.execute("select UserId from Users where Token = '{}'".format(token))
		userId = cursor.fetchone()['UserId']
		cursor.execute("insert into GroupMembers (GroupId,UserId,GroupAdmin) values ('{}','{}','{}')".format(groupId,userId,int(False)))
		cnx.commit()
		cursor.close()
		cnx.close()
		return jsonify(Code="200",Description="Adicionado ao grupo com sucesso!")
	if request.method=='DELETE':
		token = request.headers.get("auth")
		cursor.execute("select UserId from Users where Token = '{}'".format(token))
		userId = cursor.fetchone()['UserId']
		cursor.execute("select GroupAdmin from GroupMembers where GroupId = '{}' and UserId='{}'".format(groupId,userId))
		admin = cursor.fetchone()['GroupAdmin']
		if admin==1:
			cursor.execute("select * from GroupMembers where GroupId = '{}' ".format(groupId))
			alone = cursor.fetchall()
			print(len(alone))		
			if len(alone) == 1:
				cursor.execute("delete from GroupMembers where GroupId = '{}' and UserId='{}'".format(groupId,userId))
				cnx.commit()
				cursor.execute("delete from RunGroups where GroupId = '{}' ".format(groupId))
				cnx.commit()
				cursor.close()
				cnx.close()				
				return jsonify(Code="200",Description="Grupo eliminado com sucesso!")
			else:
				return jsonify(Code="409",Description="Não pode sair do próprio grupo")
		print(groupId)
		print(userId)
		cursor.execute("delete from GroupMembers where GroupId = '{}' and UserId='{}'".format(groupId,userId))
		cnx.commit()
		cursor.close()
		cnx.close()
		return jsonify(Code="200",Description="Saiu do grupo com sucesso!")


@app.route('/groups/<groupId>/members/<userId>', methods=['DELETE'])
def delGroupMember(groupId=None,userId=None):
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	cursor=cnx.cursor(dictionary=True)
	token = request.headers.get("auth")
	cursor.execute("select UserId from Users where Token = '{}'".format(token))
	adminId = cursor.fetchone()['UserId']
	cursor.execute("select GroupAdmin from GroupMembers where GroupId = '{}' and UserId='{}'".format(groupId,userId))
	ownerId = cursor.fetchone()['GroupAdmin']
	if adminId==ownerId:
			cursor.execute("delete from GroupMembers where GroupId = '{}' and UserId='{}'".format(groupId,userId))
			cnx.commit()
			cursor.close()
			cnx.close()
			return jsonify(Code="200",Description="Removeu o utilizador com sucesso!")
	return jsonify(code="400",Description="Alguma coisa não correu bem!")


@app.route('/groups/<groupId>/posts', methods=['GET','POST'])
@app.route('/posts', methods=['GET'])
def getPosts(groupId=None):
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	cursor=cnx.cursor(dictionary=True)
	if(groupId is None):		
		cursor.execute("select * from Posts")
		posts=cursor.fetchall()
		cursor.close()
		cnx.close()
		return jsonify(posts)			
	else:
		if request.method=='GET':
			cursor.execute("select po.PostId, po.Text, po.CreatedDate, po.UserId, po.GroupId, po.PhotoId, p.PathToPhoto as UserPhotoPath, ph.PathToPhoto as PostPhotoPath, u.Username from Posts po left join Users u on u.UserId=po.UserId left join Photos p on p.PhotoId=u.PhotoId left join Photos ph on ph.PhotoId=po.PhotoId where po.GroupId='{}' order by po.CreatedDate desc".format(groupId))
			posts=cursor.fetchall()
			cursor.close()
			cnx.close()
			return jsonify(posts)
		if request.method=='POST':
			args = request.args
			token = request.headers.get("auth")
			cursor.execute("select UserId from Users where Token = '{}'".format(token))
			userId = cursor.fetchone()['UserId']
			if userId is None:
				cursor.close()
				cnx.close()
				return jsonify(Code="404",Description="Token not found!")
			else:
				text = args.get("text")
				createdDate=datetime.now()
				cursor.execute("select * from GroupMembers where userid = '{}' and groupid = '{}'".format(userId, groupId))
				query = cursor.fetchall()
				if len(query) == 0:
					cursor.close()
					cnx.close()
					return jsonify(Code="403",Description="Member not in group!")
				else:
					if request.headers.get("auth") == token:
						
						if  len(request.files)>0:
							print(len(request.files))
							photoId=uploadPhotoP(request)
							cursor.execute("insert into Posts (Text,CreatedDate,UserId,GroupId,PhotoId) values ('{}','{}','{}','{}','{}')".format(text,createdDate,userId,groupId,photoId))	
						else:
							cursor.execute("insert into Posts (Text,CreatedDate,UserId,GroupId) values ('{}','{}','{}','{}')".format(text,createdDate,userId,groupId))				
						cnx.commit()
						cursor.close()
						cnx.close()
						return jsonify(Code="200",Description="Posted succesfully!!")
					else:
						cursor.close()
						cnx.close()
						return jsonify(Code="502",Description="Error!Not authorized")




@app.route('/groups/<groupId>/posts/<postId>', methods=['PUT','DELETE'])
def delPosts(groupId=None,postId=None):
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	cursor=cnx.cursor(dictionary=True)
	token = request.headers.get("auth")
	if request.method=='DELETE':
		cursor.execute("select OwnerId from RunGroups where GroupId = '{}'".format(groupId))
		owner=cursor.fetchone()['OwnerId']
		cursor.execute("select Token from Users where UserId = '{}'".format(owner))
		ownerToken = cursor.fetchone()
		if request.headers.get("auth") == token or request.headers.get("auth") == ownerToken:
			cursor.execute("delete from Posts where PostId='{}'".format(postId))			
			cnx.commit()
			cursor.close()
			cnx.close()
			return "", 200
		else:
			return "no_permission",404
	if request.method=='PUT':
		return 1
	
@app.route('/users/<userId>/posts/<postId>', methods=['PUT','DELETE'])
def delUserPosts(userId=None,postId=None):
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	cursor=cnx.cursor(dictionary=True)
	token = request.headers.get("auth")
	if request.method=='DELETE':
		cursor.execute("select Token from Users where UserId = '{}'".format(userId))
		if request.headers.get("auth") == token:
			cursor.execute("delete from Posts where PostId='{}'".format(postId))			
			cnx.commit()
			cursor.close()
			cnx.close()
			return "", 200
		else:
			return "no_permission",404
	if request.method=='PUT':
		return 1


@app.route('/groupMembers', methods=['GET'])
def getGroupsMembers():
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	cursor=cnx.cursor(dictionary=True)
	cursor.execute("select * from GroupMembers")
	groupMembers=cursor.fetchall()
	cursor.close()
	cnx.close()
	return jsonify(groupMembers)

@app.route('/logo')
def flask_logo():
    return current_app.send_static_file('flask-logo.png')


@app.route('/photos', methods=['GET'])
def getPhotos():
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	cursor=cnx.cursor(dictionary=True)
	cursor.execute("select * from Photos")
	photos=cursor.fetchall()
	cursor.close()
	cnx.close()
	return jsonify(photos)



@app.route('/photos/users', methods=['POST'])
def uploadPhotoU():
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	cursor=cnx.cursor(dictionary=True)
	path = os.path.realpath('.')
	token = request.headers.get("auth")
	cursor.execute("select UserId from Users where token = '{}'".format(token))
	pkId=cursor.fetchone()['UserId']
	cursor.execute("insert into Photos (PathToPhoto) values ('')")
	cnx.commit()
	cursor.execute("select PhotoId from Photos order by PhotoId desc limit 1")
	photoId=cursor.fetchone()['PhotoId']
	request.files["image"].save(path+"/static/img/{}.png".format(photoId))
	pathPhoto="http://16.170.180.240:5000/static/img/{}.png".format(photoId)
	cursor.execute("update Photos set PathToPhoto = '{}' where PhotoId = {}".format(pathPhoto,photoId))
	cnx.commit()
	cursor.execute("update Users set PhotoId = {} where UserId = {}".format(photoId,pkId))
	cnx.commit()
	cursor.close()
	cnx.close()
	return jsonify(Path="http://16.170.180.240:5000/static/img/{}.png".format(photoId))


@app.route('/photos/<groupId>', methods=['POST'])
def uploadPhotoG(groupId):
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	cursor=cnx.cursor(dictionary=True)
	path = os.path.realpath('.')
	token = request.headers.get("auth")
	cursor.execute("select UserId from Users where token = '{}'".format(token))
	pkId=cursor.fetchone()['UserId']
	cursor.execute("select OwnerId from RunGroups where GroupId={}".format(groupId))
	ownerId=cursor.fetchone()['OwnerId']
	if pkId==ownerId:
		cursor.execute("insert into Photos (PathToPhoto) values ('')")
		cnx.commit()
		cursor.execute("select PhotoId from Photos order by PhotoId desc limit 1")
		photoId=cursor.fetchone()['PhotoId']
		request.files["image"].save(path+"/static/img/{}.png".format(photoId))
		pathPhoto="http://16.170.180.240:5000/static/img/{}.png".format(photoId)
		cursor.execute("update Photos set PathToPhoto = '{}' where PhotoId = {}".format(pathPhoto,photoId))
		cnx.commit()
		cursor.execute("update RunGroups set PhotoId = {} where GroupId = {}".format(photoId,groupId))
		cnx.commit()
		cursor.close()
		cnx.close()
		return jsonify(Path="http://16.170.180.240:5000/static/img/{}.png".format(photoId))
	return "",403

def uploadPhotoP(request):
	print(request)
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	print(request.files)
	cursor=cnx.cursor(dictionary=True)
	path = os.path.realpath('.')
	cursor.execute("insert into Photos (PathToPhoto) values ('')")
	cnx.commit()
	cursor.execute("select PhotoId from Photos order by PhotoId desc limit 1")
	photoId=cursor.fetchone()['PhotoId']
	request.files["image"].save(path+"/static/img/{}.png".format(photoId))
	pathPhoto="http://16.170.180.240:5000/static/img/{}.png".format(photoId)
	cursor.execute("update Photos set PathToPhoto = '{}' where PhotoId = {}".format(pathPhoto,photoId))
	cnx.commit()
	cursor.close()
	cnx.close()
	return photoId

if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True)
