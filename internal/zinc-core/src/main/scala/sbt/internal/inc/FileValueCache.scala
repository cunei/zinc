/*
 * Zinc - The incremental compiler for Scala.
 * Copyright 2011 - 2017, Lightbend, Inc.
 * Copyright 2008 - 2010, Mark Harrah
 * This software is released under the terms written in LICENSE.
 */

package sbt
package internal
package inc

import java.io.File
import java.util.concurrent.ConcurrentHashMap

import xsbti.compile.analysis.Stamp

sealed trait FileValueCache[T] {
  def clear(): Unit
  def get: File => T
}

private[this] final class FileValueCache0[T](getStamp: File => Stamp, make: File => T)(
    implicit equiv: Equiv[Stamp])
    extends FileValueCache[T] {
  private[this] val backing = new ConcurrentHashMap[File, FileCache]

  def clear(): Unit = backing.clear()
  def get = file => {
    val ifAbsent = new FileCache(file)
    val cache = backing.putIfAbsent(file, ifAbsent)
    (if (cache eq null) ifAbsent else cache).get()
  }

  private[this] final class FileCache(file: File) {
    private[this] var stampedValue: Option[(Stamp, T)] = None
    def get(): T = synchronized {
      val latest = getStamp(file)
      stampedValue match {
        case Some((stamp, value)) if (equiv.equiv(latest, stamp)) => value
        case _                                                    => update(latest)
      }
    }

    private[this] def update(stamp: Stamp): T = {
      val value = make(file)
      stampedValue = Some((stamp, value))
      value
    }
  }
}
object FileValueCache {
  def apply[T](f: File => T): FileValueCache[T] = make(Stamper.forLastModified)(f)
  def make[T](stamp: File => Stamp)(f: File => T): FileValueCache[T] =
    new FileValueCache0[T](stamp, f)
}
