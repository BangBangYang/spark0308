package com.atguigu

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
class Search(qurey: String) extends {
//class Search(qurey: String) extends java.io.Serializable{
  def isMatch(s:String):Boolean={
      s.contains(qurey)
  }
  def getMatch1(rdd:RDD[String])={
    rdd.filter(isMatch)
  }
  def getMatch2(rdd:RDD[String])={
    val q = qurey
    rdd.filter(x => x.contains(q))
  }
}
object serializeable {

  def main(args: Array[String]): Unit = {
    //1.初始化配置信息及SparkContext
      val conf = new SparkConf().setMaster("local[*]").setAppName("serializeable")
      val sc = new SparkContext(conf)
    //2.创建一个RDD
      val rdd:RDD[String] = sc.parallelize(Array("hadoop", "spark","hive"))
    //3.创建一个Search对象
      val search = new Search("h")
    //4.运用第一个过滤函数并打印结果
      search.getMatch2(rdd).collect().foreach(println)
  }

}

