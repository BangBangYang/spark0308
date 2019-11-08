package com.bupt.spark.hbase

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat

object SparkHbaseTableOutPutFormatPut {
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
    val unit1 = unit.map(t => {
      val keyOut = new ImmutableBytesWritable()
      val put = new Put(Bytes.toBytes("spark_tablePut" + t))
      put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("count"), Bytes.toBytes(t.toString))
      keyOut.set(put.getRow)
      (keyOut, put)
    })
    unit1.saveAsNewAPIHadoopDataset(configuration)
  }
}
