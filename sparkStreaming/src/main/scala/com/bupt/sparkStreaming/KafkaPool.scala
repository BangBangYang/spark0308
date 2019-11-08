package com.bupt.sparkStreaming

import java.util.Properties

import org.apache.commons.pool2.impl.{DefaultPooledObject, GenericObjectPool}
import org.apache.commons.pool2.{BaseObject, BasePooledObjectFactory, PooledObject}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer



class KafkaProxy(brokers: String) {

  //
  private val properties = new Properties()
  properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
  properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  val kafkaConn = new KafkaProducer[String, String](properties)

  def send(topic: String, key: String, value: String): Unit = {
    kafkaConn.send(new ProducerRecord[String, String](topic, key, value))
  }

  def close(): Unit = {
    kafkaConn.close()
  }
}

class KafkaProxyFactory(brokers: String) extends BasePooledObjectFactory[KafkaProxy]{
  //创建实例
  override def create(): KafkaProxy = new KafkaProxy(brokers)
 //将池中对象封装
  override def wrap(t: KafkaProxy): PooledObject[KafkaProxy] = new DefaultPooledObject[KafkaProxy](t)
}
object KafkaPool {
  //声明池对象
  var kafkaPool: GenericObjectPool[KafkaProxy] = null

  def apply(brokers:String): GenericObjectPool[KafkaProxy]={
    if(kafkaPool == null){
      KafkaPool.synchronized{
        if(kafkaPool == null)
          kafkaPool = new GenericObjectPool(new KafkaProxyFactory(brokers))
      }
    }
    kafkaPool
  }
}

