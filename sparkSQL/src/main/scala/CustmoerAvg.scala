import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types.{DataType, DoubleType, LongType, StructType}

//弱类型UDAF
class CustmoerAvg extends UserDefinedAggregateFunction{
//函数输入时的数据结构
  override def inputSchema: StructType = {
    new StructType().add("age", LongType)
  }
//计算时的数据结构，缓冲区结构(sum, count)
  override def bufferSchema: StructType = {
    new StructType().add("sum", LongType).add("count", LongType)
  }
//函数返回的类型
  override def dataType: DataType = DoubleType
//函数是否稳定
  override def deterministic: Boolean = true
//计算之前缓冲区的初始化
  override def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer(0) = 0L
    buffer(1) = 0L
  }
//根据查询结果更新缓冲区数据
  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    buffer(0) = buffer.getLong(0) + input.getLong(0)
    buffer(1) = buffer.getLong(1) + 1
  }
 //将多个节点的缓冲区合并
  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
   //sum
    buffer1(0) = buffer1.getLong(0) + buffer2.getLong(0)
   //count
    buffer1(1) = buffer1.getLong(1) + buffer2.getLong(1)
  }
//计算
  override def evaluate(buffer: Row): Any = {
    buffer.getLong(0).toDouble / buffer.getLong(1)
  }
}

object testAvg{
  def main(args: Array[String]): Unit = {
    //0 配置sparkSession
    val conf = new SparkConf().setAppName("myFirstSparkSql").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val spark = SparkSession.builder().config(conf).getOrCreate()

    val df = spark.read.json("in/user.json")
    df.show()
    // 注册函数
    spark.udf.register("avg_name", new CustmoerAvg)

    df.createTempView("user")
    spark.sql("select avg_name(age) from user").show

  }
}
