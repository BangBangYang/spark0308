package com.bupt.spark.hbase
//1 table put
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}


object SparkHbaseTablePut {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("aa").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val ints = List(1,2,3,4,5,6,7,8,9,10)
    val unit = sc.parallelize(ints,1)
    unit.foreach(t => {
      println(t)
      val configuration = HBaseConfiguration.create()
      val connection = ConnectionFactory.createConnection(configuration)
      val table = connection.getTable(TableName.valueOf("student"))
      val put = new Put(Bytes.toBytes("spark_"+ t))
      put.addColumn(Bytes.toBytes("info"),Bytes.toBytes("count"),Bytes.toBytes(t.toString))
      table.put(put)
      table.close()
      connection.close()
    })


  }
}
