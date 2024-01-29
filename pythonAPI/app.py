from flask import Flask,request,redirect,url_for,json
from flask import jsonify
import mysql.connector
import base64
import os
from uuid import uuid4
from passlib.hash import sha256_crypt
from datetime import datetime,date,timedelta
from flask.json.provider import DefaultJSONProvider
from flask import current_app

#formatar datetime de acordo 
class UpdatedJSONProvider(DefaultJSONProvider):
	def default(self, o):
		if isinstance(o, datetime):
			return o.strftime('%d-%m-%Y %H:%M:%S')
		elif isinstance(o, date):
			return o.strftime('%d-%m-%Y')
		elif isinstance(o, timedelta):
			return str(o)
		return super().default(o)

app = Flask(__name__)
app.json.ensure_ascii = False
app.json = UpdatedJSONProvider(app)
json.provider.DefaultJSONProvider.ensure_ascii = False



@app.route('/', methods=['GET', 'POST'])
def welcome():
    return "Hello World!!!"

#fazer login
@app.route('/login',methods=['POST'])
def login():
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	args = request.args
	username=args.get("username")
	password=args.get("password")
	cursor=cnx.cursor()
	cursor.execute("select UserId,Username,Password from Users where Username='{}'".format(username)) #obtém da db o UserId, Username e a Password através do username
	conta=cursor.fetchone()
	#verifica se a conta existe
	if conta == None:
		cursor.close()
		cnx.close()
		return "",403
	else:		
		hash_pass=conta[2]
	#verifica se a password está certa, se sim, gera um novo token para o user
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

#dar login através do token guardado
@app.route('/users/auth/<token>',methods=['GET'])
def getUser(token=None):
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2',charset="utf8")
	cursor=cnx.cursor(dictionary=True)
	if(request.method=="GET"):
		cursor.execute("select u.UserId,u.Username,u.Name,u.LastName,u.BirthDate,u.CreatedDate,u.Weight,u.Height,u.Address,u.PhotoId,p.PathToPhoto PhotoPath from Users u left join Photos p on p.PhotoId=u.PhotoId where u.Token = '{}'".format(token))
		conta=cursor.fetchone()
		cursor.close()
		cnx.close()
		return jsonify(conta)

#registar uma nova conta
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
	#se o username já estiver em uso
	if conta != None:
		cursor.close()
		cnx.close()
		return jsonify(Code="409",Description="Username já existe")
	#se não estiver em uso, verificar se é admin
	else:
		if args.get("secretAdmin") == "PasswordSecreta":
			admin=True
		else:
			admin=False	
		#inserior os dados na db	
		cursor.execute("insert into Users (Admin,CreatedDate,username, password) values ({},'{}','{}','{}')".format(admin,createdDate,username,password))
		cnx.commit()
		cursor.close()
		cnx.close()
		return jsonify(Code="200",Description="Conta criada com sucesso")

#obter todos os users
@app.route('/users',methods=['GET'])
#obter/alterar um user
@app.route('/users/<userId>',methods=['GET','PUT'])
def getUsers(userId=None):
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2',charset="utf8")
	cursor=cnx.cursor(dictionary=True)
	#se não obter um Username, retorna a conta de todos os users
	if(userId is None):
		cursor.execute("select UserId,Username,Name,LastName,BirthDate,CreatedDate,Weight,Height,Address,PhotoId from Users")
		conta=cursor.fetchall()
		cursor.close()
		cnx.close()
		return jsonify(conta)
	#se obter um Username
	else:
		#se o request.method = 'GET', retorna o user com o username obtido
		if(request.method=="GET"):
			cursor.execute("select u.UserId,u.Username,u.Name,u.LastName,u.BirthDate,u.CreatedDate,u.Weight,u.Height,u.Address,u.PhotoId,p.PathToPhoto PhotoPath from Users u left join Photos p on p.PhotoId=u.PhotoId where u.Username = '{}'".format(userId))
			conta=cursor.fetchone()
			cursor.close()
			cnx.close()
			return jsonify(conta)
		#se o request.method = 'PUT', obter o token para verificar se é o próprio
		if(request.method=="PUT"):			
			token = request.headers.get("auth")
			cursor.execute("select Token from Users where Username = '{}'".format(userId))
			token2=cursor.fetchone()["Token"]			
			if(token == token2):
				#se a verificação for válida, guardar os dados que estão a ser alterados
				for value in request.json:
					if(request.json[value]==None or request.json[value]=="None"):
						request.json[value]="NULL" #inserir 'NULL' nos campos vazios
				name=request.json["Name"]
				lastName=request.json["LastName"]
				weight=request.json["Weight"]
				birthDate=request.json["BirthDate"]
				address=request.json["Address"]
				height=request.json["Height"]

				'''
				str=""
				for i in request.args:
					str+="{} = '{}'".format(i.capitalize(),request.args.get(i))
					if(i != list(request.args)[-1]):
						str+=","'''
				
				cursor.execute("update Users set Name='{}', LastName='{}', Weight={}, Address='{}', Height={} where Username = '{}'".format(name,lastName,weight,address,height,userId))
				#se for inserido data de nascimento, inserir na db
				if(request.json["BirthDate"]!="NULL"):
					cursor.execute("update Users set BirthDate=STR_TO_DATE('{}', '%d-%m-%Y %H:%i') where Username = '{}'".format(birthDate,userId))
				cnx.commit()
				cursor.close()
				cnx.close()
				return jsonify(Code="200",Description="Conta atualizada com sucesso")
			#se o user não for verificado
			else:
				cursor.close()
				cnx.close()
				return jsonify(Code="404",Description="Erro ao atualizar conta!")

#obter os grupos de um user			
@app.route('/users/<userId>/groups',methods=['GET'])
def getUserGroups(userId=None):	
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2',charset="utf8")
	cursor=cnx.cursor(dictionary=True)
	cursor.execute("select Name,g.GroupId as GroupId,g.CreatedDate as CreatedDate,g.City as City,g.OwnerId as OwnerId, p.PathToPhoto as PhotoPath from RunGroups g inner join  GroupMembers gm on g.GroupId=gm.GroupId left join Photos p on p.PhotoId=g.PhotoId where gm.userId={}".format(userId))
	groups=cursor.fetchall()
	cursor.close()
	cnx.close()
	return jsonify(groups)

#obter os grupos de um user	
@app.route('/users/<userId>/posts',methods=['GET'])
def getUserPosts(userId=None):	
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2',charset="utf8")
	cursor=cnx.cursor(dictionary=True)
	cursor.execute("select po.PostId, po.Text, po.CreatedDate, po.UserId, po.GroupId, po.PhotoId, ph.PathToPhoto PostPhotoPath, g.Name, u.Username, f.PathToPhoto UserPhotoPath from Posts po left join Photos ph on ph.PhotoId=po.PhotoId left join RunGroups g on g.GroupId=po.GroupId left join Users u on u.UserId=po.UserId left join Photos f on f.PhotoId=u.PhotoId where po.UserId='{}' order by PostId desc".format(userId))
	groups=cursor.fetchall()
	cursor.close()
	cnx.close()
	return jsonify(groups)
'''
@app.route('/database', methods=['GET'])
def getDB():
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2',charset="utf8")
	cursor=cnx.cursor()
	cursor.execute("show tables")
	reply=cursor.fetchall()
	cursor.close()
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
	return jsonify(list(map(str,describe)))
	'''

#obter/criar grupos
@app.route('/groups', methods=['GET','POST'])
#obter/alterar/apagar um grupo
@app.route('/groups/<groupId>', methods=['GET','PUT','DELETE'])
def getGroups(groupId=None):
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2',charset="utf8")
	cursor=cnx.cursor(dictionary=True)
	#se não obter um groupId
	if(groupId is None):
		#se o request.method='get", obter todos os grupos
		if request.method=='GET':		
			cursor.execute("select g.City,g.CreatedDate,g.GroupId,g.Name,g.OwnerId,p.PathToPhoto as PhotoPath from RunGroups g left join Photos p on g.PhotoId=p.PhotoId")
			group=cursor.fetchall()
			cursor.close()
			cnx.close()
			return jsonify(group)
		#se o request.method='post", criar um grupo com os dados fornecidos (name,city)
		if request.method=='POST':
			args = request.args
			name = args.get("name")
			city=args.get("city")
			createdDate=datetime.now()
			cursor.execute("select Name from RunGroups where LOWER(REPLACE(Name,' ','')) = LOWER(REPLACE('{}',' ',''))".format(name))
			groupName = cursor.fetchone()	
			#verifica se o nome do grupo já está em uso		
			if groupName is not None:
				cursor.close()
				cnx.close()
				return jsonify(Code="403",Description="Nome do grupo já existe!")		
			token = request.headers.get("auth")
			cursor.execute("select UserId from Users where Token = '{}'".format(token))
			userId = cursor.fetchone()
			#verifica se o token corresponde ao user
			if userId is None:
				cursor.close()
				cnx.close()
				return "token_not_found", 404
			#se corresponder, cria o grupo, e insere o user como Owner do grupo
			else:
				ownerId=userId['UserId']
				cursor.execute("insert into RunGroups (Name,City,OwnerId,CreatedDate) values ('{}','{}','{}','{}')".format(name,city,ownerId,createdDate))			
				cnx.commit()
				#após criar o grupo, obtem o groupId atribuido e insere na tabela GroupMembers com GroupAdmin=1
				cursor.execute("select GroupId from RunGroups where Name like '{}'".format(name))
				groupId = cursor.fetchone()['GroupId']
				cursor.execute("insert into GroupMembers (GroupId,UserId,GroupAdmin) values ('{}','{}','{}')".format(groupId,ownerId,int(True)))
				cnx.commit()
				cursor.close()
				cnx.close()
				return jsonify(Code="200",Description="Grupo criado com sucesso!")
		cursor.close()
		cnx.close()		
		return "",404
	
	#se for obtido um groupId
	else:
		#e se o request.method='get', obtém os dados do grupo
		if(request.method=="GET"):
			cursor.execute("select * from RunGroups where GroupId ='{}'".format(groupId))
			group=cursor.fetchone()
			cursor.close()
			cnx.close()
			return jsonify(group)
		'''
		#se o request.method='put', alterar o grupo
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
				cursor.close()
				cnx.close()
				return "",404
		'''			
		#se o request.method='delete', apagar o grupo, se o user for o Owner
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
				cursor.close()
				cnx.close()
				return "No_Permission",403
		else:
			cursor.close()
			cnx.close()
			return "",404

#obter/adicionar/remover membros de um grupo
@app.route('/groups/<groupId>/members', methods=['GET','POST','DELETE'])
def getGroupMembers(groupId=None):
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	cursor=cnx.cursor(dictionary=True)
	#se o request.method='get', obtém os users do grupo	
	if request.method=='GET':	
		cursor.execute("select u.UserId,u.Username,u.Name,u.LastName,u.BirthDate,u.CreatedDate,u.Weight,u.Height,u.Address,u.PhotoId,p.PathToPhoto PhotoPath from GroupMembers g inner join Users u on g.UserId = u.UserId left join Photos p on p.PhotoId=u.PhotoId where g.GroupId='{}'".format(groupId))
		groupMembersId=cursor.fetchall()
		cursor.close()
		cnx.close()
		return jsonify(groupMembersId)
	#se o request.method='post', adiciona o user ao grupo				
	if request.method=='POST':
		token = request.headers.get("auth")
		cursor.execute("select UserId from Users where Token = '{}'".format(token))
		userId = cursor.fetchone()['UserId']
		cursor.execute("insert into GroupMembers (GroupId,UserId,GroupAdmin) values ('{}','{}','{}')".format(groupId,userId,int(False)))
		cnx.commit()
		cursor.close()
		cnx.close()
		return jsonify(Code="200",Description="Adicionado ao grupo com sucesso!")
	##se o request.method='delete', remove o user do grupo (se for o owner, apenas se for o único no grupo)	
	if request.method=='DELETE':
		token = request.headers.get("auth")
		cursor.execute("select UserId from Users where Token = '{}'".format(token))
		userId = cursor.fetchone()['UserId']
		cursor.execute("select GroupAdmin from GroupMembers where GroupId = '{}' and UserId='{}'".format(groupId,userId))
		admin = cursor.fetchone()['GroupAdmin']
		if admin==1:
			cursor.execute("select * from GroupMembers where GroupId = '{}' ".format(groupId))
			alone = cursor.fetchall()		
			if len(alone) == 1:
				cursor.execute("delete from GroupMembers where GroupId = '{}' and UserId='{}'".format(groupId,userId))
				cnx.commit()
				cursor.execute("delete from RunGroups where GroupId = '{}' ".format(groupId))
				cnx.commit()
				cursor.close()
				cnx.close()				
				return jsonify(Code="200",Description="Grupo eliminado com sucesso!")
			else:
				cursor.close()
				cnx.close()
				return jsonify(Code="409",Description="Não pode sair do próprio grupo")
		cursor.execute("delete from GroupMembers where GroupId = '{}' and UserId='{}'".format(groupId,userId))
		cnx.commit()
		cursor.close()
		cnx.close()
		return jsonify(Code="200",Description="Saiu do grupo com sucesso!")

#remover um outro user do grupo
@app.route('/groups/<groupId>/members/<userId>', methods=['DELETE'])
def delGroupMember(groupId=None,userId=None):
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	cursor=cnx.cursor(dictionary=True)
	token = request.headers.get("auth")
	cursor.execute("select UserId from Users where Token = '{}'".format(token))
	ownerId = cursor.fetchone()['UserId']
	cursor.execute("select GroupAdmin from GroupMembers where GroupId = '{}' and UserId='{}'".format(groupId,ownerId))
	owner = cursor.fetchone()['GroupAdmin']
	if owner==1:
		cursor.execute("delete from GroupMembers where GroupId = '{}' and UserId='{}'".format(groupId,userId))
		cnx.commit()
		cursor.close()
		cnx.close()
		return jsonify(Code="200",Description="Removeu o utilizador com sucesso!")
	cursor.close()
	cnx.close()
	return jsonify(code="400",Description="Alguma coisa não correu bem!")

#obter/criar posts num grupo
@app.route('/groups/<groupId>/posts', methods=['GET','POST'])
#obter todos os posts
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
		##se o request.method='get', obtém os posts de um grupo
		if request.method=='GET':
			cursor.execute("select po.PostId, po.Text, po.CreatedDate, po.UserId, po.GroupId, po.PhotoId, p.PathToPhoto as UserPhotoPath, ph.PathToPhoto as PostPhotoPath, u.Username from Posts po left join Users u on u.UserId=po.UserId left join Photos p on p.PhotoId=u.PhotoId left join Photos ph on ph.PhotoId=po.PhotoId where po.GroupId='{}' order by po.CreatedDate desc".format(groupId))
			posts=cursor.fetchall()
			cursor.close()
			cnx.close()
			return jsonify(posts)
		##se o request.method='post', cria um post no grupo	
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
				#verifica se o user está no grupo
				if len(query) == 0:
					cursor.close()
					cnx.close()
					return jsonify(Code="403",Description="Member not in group!")
				else:
					if request.headers.get("auth") == token:
						#verifica se o post inclui uma imagem, e insere na db o post
						if  len(request.files)>0:
							photoId=uploadPhotoP(request)
							cursor.execute("insert into Posts (Text,CreatedDate,UserId,GroupId,PhotoId) values ('{}','{}','{}','{}','{}')".format(text,createdDate,userId,groupId,photoId))	
						else:
							cursor.execute("insert into Posts (Text,CreatedDate,UserId,GroupId) values ('{}','{}','{}','{}')".format(text,createdDate,userId,groupId))				
						cnx.commit()
						cursor.close()
						cnx.close()
						return jsonify(Code="200",Description="Post criado com sucesso!")
					else:
						cursor.close()
						cnx.close()
						return jsonify(Code="502",Description="Sem autorização!")



#apaga o post do grupo
@app.route('/groups/<groupId>/posts/<postId>', methods=['DELETE'])
def delPosts(groupId=None,postId=None):
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	cursor=cnx.cursor(dictionary=True)
	token = request.headers.get("auth")
	cursor.execute("select UserId from Users where Token = '{}'".format(token))
	userId = cursor.fetchone()['UserId']
	cursor.execute("select OwnerId from RunGroups where GroupId = '{}'".format(groupId))
	groupOwner=cursor.fetchone()['OwnerId']
	cursor.execute("select UserId from Posts where PostId = '{}'".format(postId))
	postOwner=cursor.fetchone()['UserId']
	#verifica se é o user que o publicou ou se é o owner do grupo
	if userId == postOwner or userId == groupOwner:
		##se o request.method='delete', remove o post do grupo	
		if request.method=='DELETE':
			cursor.execute("select PhotoId from Posts where PostId='{}'".format(postId))			
			photoId=cursor.fetchone()['PhotoId']						
			cursor.execute("delete from Posts where PostId='{}'".format(postId))			
			cnx.commit()
			cursor.execute("delete from Photos where PhotoId='{}'".format(photoId))			
			cnx.commit()
			cursor.close()
			cnx.close()
			return jsonify(Code="200",Description="Post eliminado com sucesso!")
	else:
			cursor.close()
			cnx.close()
			return jsonify(Code="403",Description="Sem Permissão!")		

#remove o post do user
@app.route('/users/<userId>/posts/<postId>', methods=['DELETE'])
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
			cursor.close()
			cnx.close()
			return "no_permission",404


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

#obter todas as fotos
@app.route('/photos', methods=['GET'])
def getPhotos():
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	cursor=cnx.cursor(dictionary=True)
	cursor.execute("select * from Photos")
	photos=cursor.fetchall()
	cursor.close()
	cnx.close()
	return jsonify(photos)


#adiciona foto ao user
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

#adiciona foto ao grupo
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
	cursor.close()
	cnx.close()
	return "",403

#adiciona fotos à db
def uploadPhotoP(request):
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
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



#obter/criar corridas de um grupo
@app.route('/groups/<groupId>/runs', methods=['GET','POST'])
#apagar uma corrida de um grupo
#@app.route('/groups/<groupId>/runs/<runId>', methods=['DELETE'])
def getRuns(groupId=None,runId=None):
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	cursor=cnx.cursor(dictionary=True)
	#se obter um runId
	if(runId is None):	
		#e se request.method='get', obter todas as corridas de um grupo
		if request.method=='GET':
			cursor.execute("select r.RunId, r.Distance, r.CreatedDate, r.UserId, r.GroupId, r.Rating, r.Time, pr.PathToPhoto as PhotoPath, pu.PathToPhoto UserPhotoPath, u.Username from Run r left join Photos pr on pr.PhotoId=r.PhotoId left join Users u on u.UserId=r.UserId left join Photos pu on u.PhotoId=pu.PhotoId where GroupId='{}' order by Rating Desc".format(groupId))
			runs=cursor.fetchall()
			cursor.close()
			cnx.close()
			return jsonify(runs)	
		#se request.method='post', adiciona uma corrida ao grupo		
		if request.method=='POST':
			args = request.args
			distance = args.get("distance")
			hours = args.get("hours")
			minutes = args.get("minutes")
			seconds = args.get("seconds")
			totalTime=(int(hours)*60)+int(minutes)+(int(seconds)/60)
			time = hours+':'+minutes+':'+seconds
			rating = (float(distance))*1000/totalTime/10.0
			createdDate=datetime.now()
			token = request.headers.get("auth")
			cursor.execute("select UserId from Users where Token='{}'".format(token))
			userId=cursor.fetchone()['UserId']
			#verifica se a corrida inclui foto, e insere na db			
			if  len(request.files)>0:
				photoId=uploadPhotoP(request)
				cursor.execute("insert into Run (Distance,Time,CreatedDate,UserId,GroupId,Rating,PhotoId) values ('{}','{}','{}','{}','{}','{}','{}')".format(distance,time,createdDate,userId,groupId,rating,photoId))	
			else:
				cursor.execute("insert into Run (Distance,Time,CreatedDate,UserId,GroupId,Rating) values ('{}','{}','{}','{}','{}','{}')".format(distance,time,createdDate,userId,groupId,rating))	
			cnx.commit()
			cursor.close()			
			cnx.close()
			return jsonify(Code="200",Description="Corrida inserida com sucesso!")
	cursor.close()
	cnx.close()
	return jsonify(Code="400",Description="Alguma coisa correu mal!")

if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True)
