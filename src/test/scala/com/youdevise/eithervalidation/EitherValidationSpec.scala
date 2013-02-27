package com.youdevise.eithervalidation

import org.specs2.Specification

import com.youdevise.eithervalidation.EitherValidation.Implicits._

class EitherValidationSpec extends Specification {
  def is =
    "Using a curried function and explicit apply method" ^
      "add(x, y)" ^
        "should return a right containing sum when both are rights" ! {
          Right(curriedAdd).apply(Right(2)).apply(Right(3)) must_== Right(5)
        } ^
        "should return a left containing all failures in a list when both are lefts containing lists" ! {
          Right(curriedAdd).apply(Left(List("a"))).apply(Left(List("b"))) must_== Left(List("a", "b"))
        } ^
        "should return a left containing all failures in a string when both are lefts containing strings" ! {
          Right(curriedAdd).apply(Left("a")).apply(Left("b")) must_== Left("ab")
        } ^
        "should return a left containing all failures in a string when both are lefts containing arrays" ! {
          Right(curriedAdd).apply(Left(Array("a"))).apply(Left(Array("b"))).left.map(_.toList) must_== Left(Array("a", "b")).left.map(_.toList)
        } ^
        bt ^
      "Person(age, name, postcode)" ^
        "should return a right containing fields when all validate" ! {
          Right(curriedPersonConstructor).apply(validAge("42")).apply(validName("Arthur")).apply(validPostcode("1234")) must_==
            Right(Person(42, "Arthur", "1234"))
        } ^
        "should return a left containing all failures" ! {
          Right(curriedPersonConstructor).apply(validAge("150")).apply(validName("dude")).apply(validPostcode("a1")) must_==
            Left(List("Age must be less than 130", "Name must begin with a capital letter", "Postcode must be 4 digits"))
        } ^
        "should return a left containing all failures as a string, when failures are strings" ! {
          Right(curriedPersonConstructor).apply(validAge2("150")).apply(validName2("dude")).apply(validPostcode2("a1")) must_==
            Left("Age must be less than 130\nName must begin with a capital letter\nPostcode must be 4 digits\n")
        } ^
        bt ^
      bt ^
    br ^
    "Using a curried function and parentheses sugar for apply method" ^
      "add(x, y)" ^
        "should return a right containing sum when both are rights" ! {
          Right(curriedAdd)(Right(2))(Right(3)) must_== Right(5)
        } ^
        "should return a left containing all failures in a list when both are lefts containing lists" ! {
          Right(curriedAdd)(Left(List("a")))(Left(List("b"))) must_== Left(List("a", "b"))
        } ^
        "should return a left containing all failures in a string when both are lefts containing strings" ! {
          Right(curriedAdd)(Left("a"))(Left("b")) must_== Left("ab")
        } ^
        "should return a left containing all failures in a string when both are lefts containing arrays" ! {
          Right(curriedAdd)(Left(Array("a")))(Left(Array("b"))).left.map(_.toList) must_== Left(Array("a", "b")).left.map(_.toList)
        } ^
        bt ^
      "Person(age, name, postcode)" ^
        "should return a right containing fields when all validate" ! {
          Right(curriedPersonConstructor)(validAge("42"))(validName("Arthur"))(validPostcode("1234")) must_==
            Right(Person(42, "Arthur", "1234"))
        } ^
        "should return a left containing all failures" ! {
          Right(curriedPersonConstructor)(validAge("150"))(validName("dude"))(validPostcode("a1")) must_==
            Left(List("Age must be less than 130", "Name must begin with a capital letter", "Postcode must be 4 digits"))
        } ^
        "should return a left containing all failures as a string, when failures are strings" ! {
          Right(curriedPersonConstructor)(validAge2("150"))(validName2("dude"))(validPostcode2("a1")) must_==
            Left("Age must be less than 130\nName must begin with a capital letter\nPostcode must be 4 digits\n")
        } ^
    end

  // A simple function to lift into an Either
  def add(x: Int, y: Int): Int = x + y

  // Tony Morris's example Person with validations, see:
  //   http://applicative-errors-scala.googlecode.com/svn/artifacts/0.6/pdf/index.pdf
  case class Person(age: Int, name: String, postcode: String)

  def validAge(s: String): Either[List[String], Int] =
    try {
      val n = s.toInt
      if (n < 0)
        Left(List("Age must be greater than 0"))
      else if (n > 130)
        Left(List("Age must be less than 130"))
      else
        Right(n)
    } catch {
      case e: Throwable => Left(List(e.toString))
    }

  def validName(s: String): Either[List[String], String] =
    if (s.headOption exists (_.isUpper))
      Right(s)
    else
      Left(List("Name must begin with a capital letter"))

  def validPostcode(s: String): Either[List[String], String] =
    if (s.length == 4 && s.forall(_.isDigit))
      Right(s)
    else
      Left(List("Postcode must be 4 digits"))

  def mapLeftListOfStringToStringWithNewline[A](e: Either[List[String], A]): Either[String, A] =
    e.left.map(_.head + "\n")

  // Adapt the above validations to return String instead of List[String]
  def validAge2(s: String): Either[String, Int] = mapLeftListOfStringToStringWithNewline(validAge(s))
  def validName2(s: String): Either[String, String] = mapLeftListOfStringToStringWithNewline(validName(s))
  def validPostcode2(s: String): Either[String, String] = mapLeftListOfStringToStringWithNewline(validPostcode(s))

  // Curried versions of the functions we'll be lifting into Eithers
  val curriedAdd = (add _).curried
  val curriedPersonConstructor = (Person.apply _).curried
}

