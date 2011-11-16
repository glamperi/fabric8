/*
 * Copyright (C) 2010-2011, FuseSource Corp.  All rights reserved
 *
 *    http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license, a copy of which has been included with this distribution
 * in the license.txt file
 */

package org.fusesource.fabric.apollo.amqp.protocol.utilities.link

import org.scalatest.matchers.ShouldMatchers
import org.fusesource.fabric.apollo.amqp.codec.types.Flow
import org.apache.activemq.apollo.util.FunSuiteSupport

class LinkFlowControlTrackerTest extends FunSuiteSupport with ShouldMatchers {
  
  test("Track with no link credit") {
    val tracker = new LinkFlowControlTracker
    var have_no_credit = false
    var visited = false
    tracker.track {visited = true} {have_no_credit = true}
    have_no_credit should be (true)
    visited should be (false)
  }
  
  test("Track with link credit") {
    val tracker = new LinkFlowControlTracker
    tracker.sender_flow(new Flow(0L, 0L, 0L, 0L, 0L, 0L, 1L, 0L, false))
    var have_no_credit = false
    var visited = false
    tracker.track {visited = true} {have_no_credit = true}
    have_no_credit should be (false)
    visited should be (true)
    tracker.link_credit should be (0L)
    tracker.delivery_count should be (1L)
    tracker.available should be (0L)
  }
  
  test("Track several units, run out of link credit") {
    val tracker = new LinkFlowControlTracker
    tracker.sender_flow(new Flow(0L, 0L, 0L, 0L, 0L, 0L, 5L, 0L, false))
    var sent = 0
    var not_sent = 0
    def go(i:Int,  max:Int): Unit = {
      tracker.track {sent = sent + 1} { not_sent = not_sent + 1}
      if (i < max) {
        go(i + 1, max)
      }
    }
    go(0, 10)
    sent should be (5)
    not_sent should  be (5)
    tracker.link_credit should be (0L)
    tracker.delivery_count should be (5L)
    tracker.available should be (5L)
  }

}