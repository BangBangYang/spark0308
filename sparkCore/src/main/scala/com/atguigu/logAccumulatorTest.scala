package com.atguigu

import java.util

import org.apache.spark.util.AccumulatorV2
import org.apache.spark.{SparkConf, SparkContext}
import scala.collection.JavaConversions._

class logAccumulatorTest extends AccumulatorV2[String,java.util.Set[String]]{

  private val _logArr: java.util.Set[String] = new java.util.HashSet[String]()

  override def isZero: Boolean = _logArr.isEmpty

  override def copy(): AccumulatorV2[String, util.Set[String]] = {
    val newlog = new  logAccumulatorTest
    newlog._logArr.addAll(_logArr)
    newlog
  }

  override def reset(): Unit = _logArr.clear()

  override def add(v: String): Unit = _logArr.add(v)

  override def merge(other: AccumulatorV2[String, util.Set[String]]): Unit = {
    _logArr.addAll(other.value)
  }

  override def value: util.Set[String] = _logArr
}
object logAcc{
  def main(args: Array[String]): Unit = {
    val conf=new SparkConf().setAppName("LogAccumulator").setMaster("local[*]")
    val sc=new SparkContext(conf)

    val logAcc = new logAccumulatorTest
    sc.register(logAcc, "logAccumlator")

    val sum = sc.parallelize(Array("1", "2a", "3", "4b", "5", "6", "7cd", "8", "9"), 2).filter( line => {
      val pattern = """^-?(\d+)"""
      val flag = line.matches(pattern)
      if(!flag){
        logAcc.add(line)
      }
      flag
    }).map(_.toInt).reduce(_+_)

    println("sum:"+sum)
    for(v <- logAcc.value) println(v)

//    for(v <- logAcc.value){
//      println(v)
//    }
  }
}
