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

EndPointをtoServiceで変換するとfinagleで受け取れるようになる。

## Endpointとは



