from flask import Flask,request,redirect,url_for
from flask import jsonify
import mysql.connector
from uuid import uuid4
app = Flask(__name__)



@app.route('/', methods=['GET', 'POST'])
def welcome():
    return "Hello World!!!"


@app.route('/login',methods=['GET','POST'])
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




if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True)