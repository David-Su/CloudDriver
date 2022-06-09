package cloud.manager

import org.apache.ibatis.io.Resources
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder

object MyBatisManager {
    val sessionFactory: SqlSessionFactory

    init {
        //����mybatis���������ļ�
        val inputStream = Resources.getResourceAsStream("mybatis_config.xml")
        //��ȡSqlSessionFactory����
        sessionFactory = SqlSessionFactoryBuilder().build(inputStream).also { factory ->
            //��ʼ�����б�
            val session = factory.openSession()
            session.update("createUserTable")
            session.close()
        }
    }

}