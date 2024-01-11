from flask import Flask,request,redirect,url_for
from flask import jsonify
import mysql.connector
from uuid import uuid4
from passlib.hash import sha256_crypt
from datetime import datetime,date
app = Flask(__name__)



@app.route('/', methods=['GET', 'POST'])
def welcome():
    return "Hello World!!!"


@app.route('/login',methods=['POST'])
def login():
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='51.20.64.58',port='3306',  database='app')
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
		return jsonify({'username':username,
			'token': uuid4()
			}),200
	else:
		return "password errada",404


@app.route('/register',methods=['POST'])
def register():
	cnx = mysql.connector.connect(user='root', password='Teste123!',host='51.20.64.58',port='3306',  database='app')
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
	if(args.get("secret") == None or args.get("secret") != "PasswordSecreta"):
		return "",404

	cnx = mysql.connector.connect(user='root', password='Teste123!',host='51.20.64.58',port='3306',  database='app')
	cursor=cnx.cursor(dictionary=True)
	cursor.execute("select Username,Name,LastName,Address,BirthDate,CreatedDate from Users")
	conta=cursor.fetchall()
	return jsonify(conta)
	







if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True)