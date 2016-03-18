# finch
________



### finchとは

twitterの[finagle](https://github.com/twitter/finagle)の関数型プログラミングのためのラッパー



build.sbt

```scala
val baseSettings = Seq(
  libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless" % shapelessVersion,
    "org.typelevel" %% "cats-core" % catsVersion,
    "com.twitter" %% "finagle-http" % finagleVersion,
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  )
```
  >"com.chuusai" %% "shapeless" % shapelessVersion,
  
  >"org.typelevel" %% "cats-core" % catsVersion,
  
  oh shapeless and cats ...
  



## hello world

```scala
import io.finch._
import com.twitter.finagle.Http

val api: Endpoint[String] = get("hello") { Ok("Hello, World!") }

Http.serve(":8080", api.toService)
```

EndpointをtoServiceで変換するとfinagleで受け取れる(Service[Request, Response]になる)ようになる。
Endpointはsprayのdirecitveみたいなものでこれを組み合わせてapiを作っていく。




## Endpointとは

Endpointは
```scala
Request => Option[Future[Output[A]]]
```
の型の関数

* Optionはrequestがパスにマッチしているかどうかを表していてNoneなら404
* Futureは非同期の計算で実行時例外が起きると500が帰る
  * scala.util.Futureではなくtwitter.util.Futureなので注意
* Aがレスポンスの内容 


実際に使ってみる

```scala
get("hoge" :: "fuge") // => /hoge/fuge のパスにマッチ
get("hello"::param("name")) // =>/hello?name=... にマッチ
get("hello"::paramOption("name")) //=> ?nameがなくてもマッチ 
get("hello"::params("name")) //=> hello?name=...&name=...にマッチ
get("hello"::paramsNonEmpty("name")) //=> /hello　nameが一つもないとエラー
```


param("fuge").as[A]とすると age=Aの型だけマッチ


/fuge/3みたいなパスをマッチさせたい場合は
string(), boolean(), uuid(), int(), long()などがある。
```scala
get("helo"::string("name")::long("age")) // hello/[String]/Longだけにマッチ
```



`::`で合成していく
`::`で合成するとshapelessのHListになる
a :: b の場合は a にマッチかつ bにマッチするパターン
val user: Endpoint[String::Long::HNil] = string("name")::long("age")



`:+:`で合成していく
`:+:`で合成するとshapelessのCoproduct(直和)になる
a :+: b の場合は a または b にマッチするパターン


```scala
val get = get("user"::long){p: Long => Ok(...)}
val put = put("user"::long){p: Long => Ok(...)}
val del = delete("user"::long){p: Long => Ok(...)}
val api = get :+: put :+: del
```


shapeless使っているので
case classをサポートしている

パラメーターから
```scala
case class User(name: String, age: Int)
val api: Endpoint[String] = get("hello" :: Endpoint.derive[User].fromParams) { u: User => Ok(u.toString) }
```


bodyから
```scala
case class User(name: String, age: Int)
val api: Endpoint[String] = get("hello" :: body.as[User) { u: User => Ok(u.toString) }
```


finchがサポートしていない型をデコードしたい場合は
```scala
implicit val dateTimeDecoder: DecodeRequest[DateTime] =
  DecodeRequest.instance(s => Try(new DateTime(s.toLong)))
```
という `String => Try[A]`の関数を実装すれば良い。　




##まとめ
finchの関数がほとんどがEndpoint型を返すのでとにかく合成しやすい







