package com.rklaehn.abc

import algebra._
import algebra.ring._

class TotalArraySeq[@sp T] private[abc](private[abc] val elements: Array[T], val default: T) extends NoEquals {
  def apply(index: Int): T =
    if(index >= 0 && index < elements.length) elements(index)
    else default
  def withoutDefault: ArraySeq[T] = new ArraySeq[T](elements)
}

private[abc] trait TotalArraySeq0 {

  implicit def eqv[A: Eq]: Eq[TotalArraySeq[A]] = new Eq[TotalArraySeq[A]] {
    override def eqv(x: TotalArraySeq[A], y: TotalArraySeq[A]): Boolean =
      Eq.eqv(x.default, y.default) && ArrayUtil.eqv(x.elements, y.elements)
  }

  implicit def semigroup[A: Semigroup: Eq: ClassTag]: Semigroup[TotalArraySeq[A]] = new Semigroup[TotalArraySeq[A]] {
    def combine(x: TotalArraySeq[A], y: TotalArraySeq[A]): TotalArraySeq[A] = TotalArraySeq.zipWith(x, y)(Semigroup.combine)
  }

  implicit def additiveSemigroup[A: AdditiveSemigroup: Eq: ClassTag]: AdditiveSemigroup[TotalArraySeq[A]] = new AdditiveSemigroup[TotalArraySeq[A]] {
    def plus(x: TotalArraySeq[A], y: TotalArraySeq[A]): TotalArraySeq[A] = TotalArraySeq.zipWith(x, y)(AdditiveSemigroup.plus)
  }
}

private[abc] trait TotalArraySeq1 extends TotalArraySeq0 {

  implicit def order[A: Order]: Order[TotalArraySeq[A]] = new Order[TotalArraySeq[A]] {
    override def eqv(x: TotalArraySeq[A], y: TotalArraySeq[A]): Boolean =
      Eq.eqv(x.default, y.default) && ArrayUtil.eqv(x.elements, y.elements)

    override def compare(x: TotalArraySeq[A], y: TotalArraySeq[A]): Int = {
      val r = Order.compare(x.default, y.default)
      if (r != 0) r
      else ArrayUtil.vectorCompare(x.elements, x.default, y.elements, y.default)
    }
  }

  implicit def monoid[A: Monoid : Eq : ClassTag]: Monoid[TotalArraySeq[A]] = new Monoid[TotalArraySeq[A]] {
    def combine(x: TotalArraySeq[A], y: TotalArraySeq[A]): TotalArraySeq[A] = TotalArraySeq.zipWith(x, y)(Semigroup.combine)
    def empty: TotalArraySeq[A] = TotalArraySeq.constant(Monoid.empty[A])
  }

  implicit def additiveMonoid[A: AdditiveMonoid : Eq : ClassTag]: AdditiveMonoid[TotalArraySeq[A]] = new AdditiveMonoid[TotalArraySeq[A]] {
    def zero: TotalArraySeq[A] = TotalArraySeq.constant(AdditiveMonoid.zero[A])
    def plus(x: TotalArraySeq[A], y: TotalArraySeq[A]): TotalArraySeq[A] = TotalArraySeq.zipWith(x, y)(AdditiveSemigroup.plus)
  }
}

private[abc] trait TotalArraySeq2 extends TotalArraySeq1 {

  implicit def hash[A: Hash]: Hash[TotalArraySeq[A]] = new Hash[TotalArraySeq[A]] {
    override def eqv(x: TotalArraySeq[A], y: TotalArraySeq[A]): Boolean =
      Eq.eqv(x.default, y.default) && ArrayUtil.eqv(x.elements, y.elements)
    override def hash(a: TotalArraySeq[A]): Int =
      (ArrayUtil.hash(a.elements), Hash.hash(a.default)).##
  }

  implicit def group[A: Group: Eq: ClassTag]: Group[TotalArraySeq[A]] = new Group[TotalArraySeq[A]] {
    def empty: TotalArraySeq[A] = TotalArraySeq.constant(Monoid.empty[A])
    def combine(x: TotalArraySeq[A], y: TotalArraySeq[A]): TotalArraySeq[A] = TotalArraySeq.zipWith(x, y)(Semigroup.combine)
    def inverse(a: TotalArraySeq[A]): TotalArraySeq[A] = TotalArraySeq.map(a)(Group.inverse)
    override def remove(x: TotalArraySeq[A], y: TotalArraySeq[A]): TotalArraySeq[A] = TotalArraySeq.zipWith(x, y)(Group.remove)
  }

  implicit def additiveGroup[A: AdditiveGroup: Eq: ClassTag]: AdditiveGroup[TotalArraySeq[A]] = new AdditiveGroup[TotalArraySeq[A]] {
    def zero: TotalArraySeq[A] = TotalArraySeq.constant(AdditiveMonoid.zero[A])
    def plus(x: TotalArraySeq[A], y: TotalArraySeq[A]): TotalArraySeq[A] = TotalArraySeq.zipWith(x, y)(AdditiveSemigroup.plus)
    def negate(a: TotalArraySeq[A]): TotalArraySeq[A] = TotalArraySeq.map(a)(AdditiveGroup.negate)
    override def minus(x: TotalArraySeq[A], y: TotalArraySeq[A]): TotalArraySeq[A] = TotalArraySeq.zipWith(x, y)(AdditiveGroup.minus)
  }
}

private[abc] trait TotalArraySeq3 extends TotalArraySeq2 {

  implicit def semiring[A: Semiring: Eq: ClassTag]: Semiring[TotalArraySeq[A]] = new Semiring[TotalArraySeq[A]] {
    def plus(x: TotalArraySeq[A], y: TotalArraySeq[A]): TotalArraySeq[A] = TotalArraySeq.zipWith(x, y)(Semiring.plus)
    def times(x: TotalArraySeq[A], y: TotalArraySeq[A]): TotalArraySeq[A] = TotalArraySeq.zipWith(x, y)(Semiring.times)
    def zero: TotalArraySeq[A] = TotalArraySeq.constant(Semiring.zero[A])
  }
}

object TotalArraySeq extends TotalArraySeq3 {

  implicit def rig[A: Rig: Eq: ClassTag]: Rig[TotalArraySeq[A]] = new Rig[TotalArraySeq[A]] {
    override def zero: TotalArraySeq[A] = TotalArraySeq.constant(Semiring.zero[A])
    override def plus(x: TotalArraySeq[A], y: TotalArraySeq[A]): TotalArraySeq[A] = TotalArraySeq.zipWith(x, y)(Semiring.plus)
    override def times(x: TotalArraySeq[A], y: TotalArraySeq[A]): TotalArraySeq[A] = TotalArraySeq.zipWith(x, y)(Semiring.times)
    override def one: TotalArraySeq[A] = TotalArraySeq.constant(Rig.one[A])
  }

  def constant[A: ClassTag](value: A) = new TotalArraySeq[A](Array.empty[A], value)

  private[abc] def zipWith[A: Eq: ClassTag](x: TotalArraySeq[A], y: TotalArraySeq[A])(f: (A, A) => A): TotalArraySeq[A] = {
    val rd = f(x.default, y.default)
    val re = ArrayUtil.combine(x.elements, x.default, y.elements, y.default)(f)
    new TotalArraySeq[A](ArrayUtil.dropRightWhile(re, rd), rd)
  }

  private[abc] def map[A: Eq: ClassTag](a: TotalArraySeq[A])(f: A => A): TotalArraySeq[A] = {
    val rd = f(a.default)
    val re = a.elements.map(f)
    new TotalArraySeq[A](ArrayUtil.dropRightWhile(re, rd), rd)
  }
}