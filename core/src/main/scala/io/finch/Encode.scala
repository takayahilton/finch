package io.finch

import cats.Show
import cats.data.Xor
import com.twitter.io.Buf
import shapeless.Witness

/**
 * An abstraction that is responsible for encoding the value of type `A`.
 */
trait Encode[A] {
  type ContentType <: String

  def apply(a: A): Buf
}

trait LowPriorityEncodeInstances {

  type Aux[A, CT <: String] = Encode[A] { type ContentType = CT }

  type ApplicationJson[A] = Aux[A, Witness.`"application/json"`.T]
  type TextPlain[A] = Aux[A, Witness.`"text/plain"`.T]

  def instance[A, CT <: String](fn: A => Buf): Aux[A, CT] =
    new Encode[A] {
      type ContentType = CT
      def apply(a: A): Buf = fn(a)
    }

  def applicationJson[A](fn: A => Buf): ApplicationJson[A] =
    instance[A, Witness.`"application/json"`.T](fn)

  def textPlain[A](fn: A => Buf): TextPlain[A] =
    instance[A, Witness.`"text/plain"`.T](fn)

  implicit def encodeShow[A](implicit s: Show[A]): TextPlain[A] =
    textPlain(a => Buf.Utf8(s.show(a)))
}

object Encode extends LowPriorityEncodeInstances {

  class Implicitly[A] {
    def apply[CT <: String](w: Witness.Aux[CT])(implicit
      e: Encode.Aux[A, CT]
    ): Encode.Aux[A, CT] = e
  }

  @inline def apply[A]: Implicitly[A] = new Implicitly[A]

  implicit def encodeUnit[CT <: String]: Aux[Unit, CT] =
    instance(_ => Buf.Empty)

  implicit def encodeBuf[CT <: String]: Aux[Buf, CT] =
    instance(identity)

  implicit val encodeExceptionAsTextPlain: TextPlain[Exception] =
    textPlain(e => Buf.Utf8(Option(e.getMessage).getOrElse("")))

  implicit val encodeExceptionAsJson: ApplicationJson[Exception] =
    applicationJson(e => Buf.Utf8(s"""{"message": "${Option(e.getMessage).getOrElse("")}""""))

  implicit val encodeString: TextPlain[String] =
    textPlain(Buf.Utf8.apply)

  implicit def encodeXor[A, B, CT <: String](implicit
    ae: Encode.Aux[A, CT],
    be: Encode.Aux[B, CT]): Encode.Aux[A Xor B, CT] = {
    instance[A Xor B, CT](xor => xor.fold(ae.apply, be.apply))
  }
}
