/*
 * Zinc - The incremental compiler for Scala.
 * Copyright 2011 - 2017, Lightbend, Inc.
 * Copyright 2008 - 2010, Mark Harrah
 * This software is released under the terms written in LICENSE.
 */

package sbt.internal.inc

import sbinary._
import sbinary.DefaultProtocol._
import sbt.internal.inc.APIs.emptyCompanions
import xsbti.api.{ AnalyzedClass, NameHash, SafeLazyProxy }

object AnalyzedClassFormats {
  // This will throw out API information intentionally.
  def analyzedClassFormat(implicit ev0: Format[Compilation],
                          ev1: Format[NameHash]): Format[AnalyzedClass] =
    wrap[AnalyzedClass, (Long, String, Int, Array[NameHash], Boolean)](
      a => (a.compilationTimestamp(), a.name, a.apiHash, a.nameHashes, a.hasMacro),
      (x: (Long, String, Int, Array[NameHash], Boolean)) =>
        x match {
          case (compilationTimestamp: Long,
                name: String,
                apiHash: Int,
                nameHashes: Array[NameHash],
                hasMacro: Boolean) =>
            new AnalyzedClass(compilationTimestamp,
                              name,
                              SafeLazyProxy(emptyCompanions),
                              apiHash,
                              nameHashes,
                              hasMacro)
      }
    )
}
