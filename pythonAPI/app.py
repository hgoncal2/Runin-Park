from flask import Flask,request,redirect,url_for,json
from flask import jsonify
import mysql.connector
import base64
import os
from uuid import uuid4
from passlib.hash import sha256_crypt
from datetime import datetime,date
from flask.json.provider import DefaultJSONProvider

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
	params=(username,)
	cursor=cnx.cursor()
	cursor.execute("select UserId,username,password from Users where Username=%s",params)
	conta=cursor.fetchone()

	if conta == None:
		cursor.close()
		cnx.close()
		return "Conta não existe",403
	else:

		hash_pass=conta[2]
	if 	sha256_crypt.verify(password,hash_pass):
		newToken=uuid4()
		
		cursor.execute("update Users set token = '{}' where Username='{}'".format(newToken,username))
		cnx.commit()
		cursor.close()
		cnx.close()
		return jsonify({'username':username,
			'token': newToken
			}),200
	else:
		cursor.close()
		cnx.close()
		return "password errada",403


@app.route('/register',methods=['POST'])
def register():
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	args = request.args
	username=args.get("username")
	password=args.get("password")
	params=(username,)
	cursor=cnx.cursor()
	cursor.execute("select Username from Users where Username=%s",params)
	conta=cursor.fetchone()
	if conta != None:
		cursor.close()
		cnx.close()
		return "Account already exists",400
	else:
		if args.get("secretAdmin") == "PasswordSecreta":
			admin=True
		else:
			admin=False


		password=sha256_crypt.hash(args.get("password"))
		name=args.get("name")
		lastName=args.get("lastName")
		birthDate=args.get("birthDate").split("-")


		birthDate=date(int(birthDate[2]),int(birthDate[1]),int(birthDate[0]))
		birthDate.strftime('%d-%m-%Y')
		weight=args.get("weight")
		height=args.get("height")
		address=args.get("address")
		createdDate=datetime.now()
		token= uuid4()
		cursor.execute("insert into Users (Username,Password,Name,LastName,BirthDate,CreatedDate,Token,Admin) values ('{}','{}','{}','{}','{}','{}','{}','{}')".format(username,password,name,lastName,birthDate,createdDate,token,int(admin)))
		cnx.commit()
		cursor.close()
		cnx.close()
		return "",200

@app.route('/users',methods=['GET'])
@app.route('/users/<username>',methods=['GET','PUT'])
def getUsers(username=None):
	args = request.args
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2',charset="utf8")
	cursor=cnx.cursor(dictionary=True)
	if(username is None):
		cursor.execute("select UserId,Username,Name,LastName,BirthDate,CreatedDate,Weight,Height,Address from Users")
		conta=cursor.fetchall()
		cursor.close()
		cnx.close()
		return jsonify(conta)
	else:
		if(request.method=="GET"):
			cursor.execute("select UserId,Username,Name,LastName,BirthDate,CreatedDate,Weight,Height,Address from Users where username = '{}'".format(username))
			conta=cursor.fetchone()
			cursor.close()
			cnx.close()
			return jsonify(conta)
		if(request.method=="PUT"):
			token = request.headers.get("auth")
			cursor.execute("select Token from Users where username = '{}'".format(username))
			token2=cursor.fetchone()['Token']
			
			if(token == token2):
				str=""
				for i in request.args:
					str+="{} = '{}'".format(i.capitalize(),request.args.get(i))
					if(i != list(request.args)[-1]):
						str+=","
				cursor.execute("update Users set {} where username = '{}'".format(str,username))
				cnx.commit()
				cursor.close()
				cnx.close()
				return "",202
			else:
				print(request.args)
				cursor.close()
				cnx.close()
				return "",404
			
@app.route('/users/<userId>/groups',methods=['GET'])
def getUserGroups(userId=None):	
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2',charset="utf8")
	cursor=cnx.cursor()
	cursor.execute("select Name from RunGroups g inner join  GroupMembers gm on g.GroupId=gm.GroupId where gm.userId='{}'".format(userId))
	groups=cursor.fetchall()
	cursor.close()
	cnx.close()
	return jsonify(groups)

@app.route('/users/<userId>/posts',methods=['GET'])
def getUserPosts(userId=None):	
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2',charset="utf8")
	cursor=cnx.cursor()
	cursor.execute("select * from Posts where UserId='{}'".format(userId))
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
			cursor.execute("select * from RunGroups")
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
				return "groupName_exists",404		
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
				return "", 200
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
			return

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

@app.route('/groups/<groupId>/members', methods=['GET','POST'])
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
		return "Added_to_Group", 200






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
			cursor.execute("select * from Posts where GroupId='{}'".format(groupId))
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
				return "token_not_found", 404
			else:
				text = args.get("text")
				createdDate=datetime.now()
				cursor.execute("select * from GroupMembers where userid = '{}' and groupid = '{}'".format(userId, groupId))
				query = cursor.fetchall()
				if len(query) == 0:
					cursor.close()
					cnx.close()
					return "member_not_in_group",404
				else:
					if request.headers.get("auth") == token:					
						cursor.execute("insert into Posts (Text,CreatedDate,UserId,GroupId) values ('{}','{}','{}','{}')".format(text,createdDate,userId,groupId))					
						cnx.commit()
						cursor.close()
						cnx.close()
						return "", 200
					else:
						cursor.close()
						cnx.close()
						return "", 502




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





#@app.route('/group/<groupId>/posts', methods=['GET','POST'])


@app.route('/groupMembers', methods=['GET'])
def getGroupsMembers():
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	cursor=cnx.cursor(dictionary=True)
	cursor.execute("select * from GroupMembers")
	groupMembers=cursor.fetchall()
	cursor.close()
	cnx.close()
	return jsonify(groupMembers)

@app.route('/photos/<base64>', methods=['POST'])
def uploadPhoto(base64):
	with open("imageToSave.png","wb") as fh:
		fh.write(base64.decodebytes(base64))
		fh.close()
	path = os.path.realpath('.')

	return jsonify(Path=path+'/imageToSave.png')



if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True)