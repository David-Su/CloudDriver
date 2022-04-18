package cloud;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.mysql.cj.Session;

public final class MyBatisManager {

	private static MyBatisManager instance = new MyBatisManager();

	private SqlSessionFactory sessionFactory;

	private MyBatisManager() {
		try {
			//����mybatis���������ļ�
			InputStream inputStream = Resources.getResourceAsStream("mybatis_config.xml");
			//��ȡSqlSessionFactory����
			sessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
			//��ʼ�����б�
			SqlSession session= sessionFactory.openSession();
			session.update("createUserTable");
			session.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static MyBatisManager getInstance() {
		return instance;
	}
	
	public SqlSessionFactory getSessionFactory() {
		return sessionFactory;
	} 

}
