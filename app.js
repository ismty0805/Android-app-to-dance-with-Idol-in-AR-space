/*jslint devel: true */ 
/* eslint-disable no-console */ 
/*eslint no-undef: "error"*/ 
/*eslint-env node*/
//IP주소가 변화하면 안드로이드 앱 내에 있는 url 주소도 바꿔주어야 정상 동작하기시작함!


var express = require('express');
var http = require('http');
var bodyParser= require('body-parser');
var mongoClient = require('mongodb').MongoClient;
var app = express();

app.set('port',80);
app.use(bodyParser.urlencoded({extended:false}));
app.use(bodyParser.json({limit: '50mb'}));
app.use(bodyParser.urlencoded({limit: '50mb', extended: true}));

var database;
 
//몽고디비에 연결 ,  보통 웹서버 만든 직후 연결 , DB 먼저 연결 되도 상관 없음
//먼저 db를 가져온다 
function connectDB()
{
    //localhost 로컬 호스트
    //:27017  몽고디비 포트
    //local db 생성시 만든 폴더 명
    var databaseURL = 'mongodb://localhost:27017';
    mongoClient.connect(databaseURL,
        function (err, db)
        {
            if (err)
            {
                console.log('db connect error');
                return;
            }
 
            console.log('db was connected : ' + databaseURL);
            database = db;          //이 구문까지 실행되었다면 ongoDB 에 연결된 것
        }
    );
    
 
}

//첫 번째 미들웨어
app.use(function(req, res, next) {

    var firstitem = req.body[0];
    console.log(firstitem);
    var sign = firstitem.sign;
    console.log(sign);
    if(sign ==1){
         //연락처 등록
        console.log(req.body);
        addDBbyNum(req, database);

        res.send([]);
    }else if(sign == 2){
        //연락처 받아오기
        getDBContacts(req, database, 
            function(err, docs)
            {
                if(err){
                    console.log('Error!!!');
                    return;
                }
                if(docs){
                    res.send(docs);
                }
                else{
                    console.log('empty Error!!!');
                    res.send([]);
                }
            }
            );
    }
    else if(sign == 3){
        //갤러리 등록
        addDBImg(req, database)
        res.send([]);
    }
    else if(sign == 4){
        //갤러리 받아오기
        getDBGallery(req, database, 
            function(err, docs)
            {
                if(err){
                    console.log('Error!!!');
                    return;
                }
                if(docs){
                    console.log("ok sending imagearray");
                    res.send(docs);
                }
                else{
                    console.log('empty Error!!!');
                    res.send([]);
                }
            }
            );
    }else if(sign == 5){
        //갤러리 지우기
        eraseDBGallery(req, database);
        res.send([]);
    }
});
var server = http.createServer(app).listen(app.get('port'),function(){
    connectDB();
    console.log("익스프레스로 웹 서버를 실행함 : "+ app.get('port')); 
    
    
});


async function addDBbyNum(req, db){
    var cnt = await db.db("test").collection("users").count()
    console.log(cnt);
    if(cnt==0){
        db.db("test").collection("users").insertMany(req.body, function(err, doc){
            console.log("Added Contact");
            if(err) throw err;
        });
    }

}


var getDBContacts = function (req, db, callback){
    var result = db.db('test').collection('users').find();
    result.toArray(
        function(err, docs)
        {
            if(err){
                callback(err, null);
                return;
            }
            if(docs.length>0)
            {
                callback(null, docs);
            }
            else{
                callback(null, null);
            }
        }
    );
};
var getDBGallery = function (req, db, callback){
    var result = db.db('test').collection('gallery').find();
    result.toArray(
        function(err, docs)
        {
            if(err){
                callback(err, null);
                return;
            }
            if(docs.length>0)
            {
                callback(null, docs);
            }
            else{
                callback(null, null);
            }
        }
    );
};
function addDBImg(req, db){
    db.db("test").collection("gallery").insertMany(req.body, function(err, doc){
        console.log("Added Img");
        if(err) throw err;
    });

}
function eraseDBGallery(req, db){
    db.db('test').collection('gallery').removeOne({"img":req.body[0].img});
}