package com.atguigu

import org.apache.spark.{SparkConf, SparkContext}
//object mapPati{
//  def main(args: Array[String]): Unit = {
//    val conf = new SparkConf().setAppName("mp").setMaster("local")
//    val sc = new SparkContext(conf)
//    val rdd = sc.parallelize(List(("kpop","female"),("zorro","male"),("mobin","male"),("lucy","female")))
//    val res = rdd.mapPartitions(partitionsFun)
//    res.collect()
//  }
//  def partitionsFun(iter: Iterator[(String,String)]):Iterator[String]={
//    var woman = List[String]()
//    while(iter.hasNext){
//    val  next = iter.next()
//      next match {
//        case (_, "female") => woman = next._1 :: woman
//        case _ =>
//      }
//    }
//    println(woman.toString())
//    woman.iterator
//  }
//}
object mapPati {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("wc").setMaster("local")
    //2、创建sparkcontext
    val sc = new SparkContext(conf)

    val rdd = sc.parallelize(List(("kpop","female"),("zorro","male"),("mobin","male"),("lucy","female")))
    val res = rdd.mapPartitions(partitionsFun)
    println(res.collect())

  }

  def partitionsFun(iter : Iterator[(String,String)]) : Iterator[String] = {
    var woman = List[String]()
    while (iter.hasNext){
      val next = iter.next()
      next match {
        case (_,"female") => woman = next._1 :: woman
        case _ =>
      }
    }
    println(woman.toString())
    woman.iterator
  }


}
