var express = require('express');
var http = require('http');
var serveStatic = require('serve-static');      //특정 폴더의 파일들을 특정 패스로 접근할 수 있도록 열어주는 역할
var path = require('path');
var cookieParser = require('cookie-parser');
var expressSession = require('express-session');
var expressErrorHandler = require('express-error-handler');
var bodyParser = require('body-parser');
var mongoClient = require('mongodb').MongoClient;
var connected = 0;
 
 
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
                connected = 1;
                return;
            }
 
            console.log('db was connected : ' + databaseURL);
            connected = 1;
            database = db;          //이 구문까지 실행되었다면 ongoDB 에 연결된 것
        }
    );
    
 
}

var app = express();      //express 서버 객체
app.set('port', 80);
//웹서버를 app 기반으로 생성
var appServer = http.createServer(app);
appServer.listen(app.get('port'),
    function () {
        connectDB();
        console.log('express 웹서버 실행' + app.get('port'));
    }
);
app.use(bodyParser.json());

app.get('/contact', function(req, res){
    findcontact(database, function(err, docs){
        if(err){console.log('Error');}
        if(docs){res.send(docs);}
        else{
            console.log('empty Error!!!');
            res.end();
        }
    })
})

app.get('/gallery', function(req, res){
    
    findgallery(database, function(err, docs){
        if(err){console.log('Error');}
        if(docs){res.send(docs);}
        else{
            console.log('empty Error!!!');
            res.end();
        }
    })
})

app.put('/addContact', function(req, res){
    database.db("test").collection("users").insert([{"aaa": " dfafds"}], function(err, doc){
            console.log("Added");
            if(err) throw err;
        });
})
app.delete('/deleteContact', function(req, res){
    database.db("test").collection("users").remove([{"aaa": " dfafds"}], function(err, doc){
            console.log("Added");
            if(err) throw err;
        });
})
var findcontact = function(db,  callback)
{
    var users = db.db('test').collection("users");
    var result = users.find({"name":"권형근"});

    result.toArray(
        function (err, docs)
        {
            if (err) {
                callback(err, null);
                return;
            }
 
            if (docs.length > 0)
            {
                console.log('find user [ ' + docs + ' ]');
                callback(null, docs);
            }
            else
            {
                console.log('can not find user [ ' + docs + ' ]');
                callback(null, null);
            }
        }
    
    );
}
var findgallery = function(db,  callback)
{
    var gallery = db.db('test').collection("gallery");
    var result = gallery.find();

    result.toArray(
        function (err, docs)
        {
            if (err) {
                callback(err, null);
                return;
            }
 
            if (docs.length > 0)
            {
                console.log('find gallery [ ' + docs + ' ]');
                callback(null, docs);
            }
            else
            {
                console.log('can not gallery [ ' + docs + ' ]');
                callback(null, null);
            }
        }
    
    );
}


// var findDocByName = function(db, name, callback)
// {
//     var users = db.db('test').collection("users");
//     var result = users.find({'name':name});

//     result.toArray(
//         function (err, docs)
//         {
//             if (err) {
//                 callback(err, null);
//                 return;
//             }
 
//             if (docs.length > 0)
//             {
//                 console.log('find user [ ' + docs + ' ]');
//                 callback(null, docs);
//             }
//             else
//             {
//                 console.log('can not find user [ ' + docs + ' ]');
//                 callback(null, null);
//             }
//         }
    
//     );
// }
// function findByName(db, name){
//     app.get('/result', function(req, res){
//         // var name = req.body.name;
//         findDocByName(db, name, function(err, docs){
//             if(err){console.log('Error');}
//             if(docs){
//                 res.send(docs[0]);
//                 console.log(docs[0]);
//             }
//             else{
//                 console.log('empty Error!!!');
//                 res.end();
//             }
//         })
//     })
// }

// findByName(database, "김몰입");