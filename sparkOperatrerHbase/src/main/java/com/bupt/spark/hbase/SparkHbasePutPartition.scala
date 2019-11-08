package com.bupt.spark.hbase

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}

object SparkHbasePutPartition {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("aa").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val ints = List(1,2,3,4,5,6,7,8,9,10)
    val unit = sc.parallelize(ints,2)
    val configuration = HBaseConfiguration.create()
    configuration.set(TableOutputFormat.OUTPUT_TABLE, "student")
    configuration.set("mapreduce.job.outputformat.class", classOf[TableOutputFormat[ImmutableBytesWritable]].getName)
    configuration.set("mapreduce.job.output.key.class", classOf[ImmutableBytesWritable].getName)
    configuration.set("mapreduce.job.output.value.class", classOf[Put].getName)

    println(unit.getNumPartitions)
    var rdd = unit.repartition(5)
    println(rdd.getNumPartitions)

    val replication = rdd.mapPartitions(f => {
      val list = f.toList
      println(list)
     import scala.collection.mutable.ListBuffer
      val list1 = new ListBuffer[(ImmutableBytesWritable, Put)]
      val writable = new ImmutableBytesWritable()
      for(next <- list){
        val put = new Put(Bytes.toBytes("spark_part"+next))
        writable.set(put.getRow)
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("count"), Bytes.toBytes(next.toString))
        list1 += ((writable,put))
      }
      list1.toIterator
    })
    replication.saveAsNewAPIHadoopDataset(configuration)


  }
}
