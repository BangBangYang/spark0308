package com.bupt.spark.hbase
//2 table batch put
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}

object SparkHbaseTableBatchPut {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("aa").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val ints = List(1,2,3,4,5,6,7,8,9,10)
    val unit = sc.parallelize(ints,2)
    println(unit.partitions.length)
    unit.foreachPartition(f => {
      val list = f.toList
      println(list)
      val puts = new java.util.ArrayList[Put]()
      for(next <- list){
        val put = new Put(Bytes.toBytes("spark_batch"+next))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("count"), Bytes.toBytes(next.toString))
        puts.add(put)
      }
      val configuration = HBaseConfiguration.create()
      val connection = ConnectionFactory.createConnection(configuration)
      val table = connection.getTable(TableName.valueOf("student"))
      table.put(puts)
      table.close()
      connection.close()
    })
  }
}
