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

EndpointをtoServiceで変換するとfinagleで受け取れるようになる。
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
get("hello"::paramOption("name")) => ?nameがなくてもマッチ 
get("hello"::params("name"))// => hello?name=...&name=...にマッチ
get("hello"::paramsNonEmoty("name")) => /hello　nameが一つもないとエラー
```

herder














