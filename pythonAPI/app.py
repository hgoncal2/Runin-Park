from flask import Flask,request,redirect,url_for,json
from flask import jsonify
import mysql.connector
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
    return "Hellu Wurld!!!"


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
		return "Conta não existe",404
	else:

		hash_pass=conta[2]
	if 	sha256_crypt.verify(password,hash_pass):
		newToken=uuid4()
		
		cursor.execute("update Users set token = '{}' where Username='{}'".format(newToken,username))
		cnx.commit()
		return jsonify({'username':username,
			'token': newToken
			}),200
	else:
		return "password errada",404


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
		return "",200
@app.route('/users',methods=['GET'])	
def getUsers():
	args = request.args
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2',charset="utf8")
	cursor=cnx.cursor(dictionary=True)
	cursor.execute("select Username,Name,LastName,BirthDate,CreatedDate from Users")
	conta=cursor.fetchall()
	print(conta[1]['BirthDate'])
	return jsonify(conta)

@app.route('/user/<username>',methods=['GET'])	
def getUser(username):
	args = request.args
	print(username)
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	cursor=cnx.cursor(dictionary=True)
	
	cursor.execute("select Username,Name,LastName,BirthDate,CreatedDate from Users where username = '{}'".format(username))
	conta=cursor.fetchone()
	return jsonify(conta)
	

@app.route('/groups', methods=['GET','POST'])
def getGroups():
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2',charset="utf8")
	cursor=cnx.cursor(dictionary=True)
	if request.method=='GET':		
		cursor.execute("select * from RunGroups")
		posts=cursor.fetchall()
		return jsonify(posts)

	if request.method=='POST':
		args = request.args
		name = args.get("name")
		city=args.get("city")
		createdDate=datetime.now()
		print(createdDate)
		cursor.execute("select Name from RunGroups where LOWER(REPLACE(Name,' ','')) = LOWER(REPLACE('{}',' ',''))".format(name))
		groupName = cursor.fetchone()
		if groupName is not None:
			return "groupName_exists",404		
		token = request.headers.get("auth")
		cursor.execute("select UserId from Users where Token = '{}'".format(token))
		userId = cursor.fetchone()
		if userId is None:
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

			return "", 200
	return "",404


#@app.route('/group/<groupId>')
#def addToGroup(groupId):
#	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
#	cursor=cnx.cursor(dictionary=True)
#	args = request.args
#	groupId=(groupId)
#	cursor.execute("select Username,Name,LastName,BirthDate,CreatedDate from Users where username =%s",params)
#	conta=cursor.fetchone()
#	return jsonify(conta)





@app.route('/posts', methods=['GET'])
def getPosts():
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='16.170.180.240',port='3306',  database='app2')
	cursor=cnx.cursor(dictionary=True)
	if request.method=='GET':		
		cursor.execute("select * from Posts")
		posts=cursor.fetchall()
		return jsonify(posts)	

	if request.method=='POST':
		args = request.args
		text = args.get("text")
		createdDate=datetime.now()
		userId = args.get("userID")
		groupId = args.get("groupID")
		photoId = args.get("photoid")
		cursor.execute("select * from GroupMembers where userid = {} and groupid = {}".format(userId, groupId))
		query = cursor.fetchone()
		if query.rowcount == 0:
			return "",404
		else:
			cursor.execute("select Token from Users where UserId = {}".format(userId))
			token = cursor.fetchone()
			if request.headers.get("auth") == token:
				cursor.execute("insert into Posts (Text,CreatedDate,UserId,GroupId,PhotoId) values ('{}','{}','{}','{}','{}')"
				.format(text,createdDate,userId,groupId,photoId))
				cnx.commit()
				return "", 200
			else:
				return "", 502





#@app.route('/group/<groupId>/posts', methods=['GET','POST'])

#@app.route('/user/posts')






if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True)