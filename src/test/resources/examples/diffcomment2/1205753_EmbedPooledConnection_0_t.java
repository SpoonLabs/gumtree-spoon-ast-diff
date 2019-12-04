 /*
 
    Derby - Class org.apache.derby.jdbc.EmbedPooledConnection
 
    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
  */
 
 package org.apache.derby.jdbc;
 
 import org.apache.derby.iapi.services.sanity.SanityManager;
 import org.apache.derby.iapi.reference.Property;
 import org.apache.derby.iapi.error.ExceptionSeverity;
 import org.apache.derby.iapi.sql.conn.LanguageConnectionContext;
 
 /* import impl class */
 import org.apache.derby.impl.jdbc.Util;
 import org.apache.derby.impl.jdbc.EmbedConnection;
 import org.apache.derby.iapi.jdbc.BrokeredConnection;
 import org.apache.derby.iapi.jdbc.BrokeredConnectionControl;
 import org.apache.derby.iapi.jdbc.EngineConnection;
 import org.apache.derby.impl.jdbc.EmbedPreparedStatement;
 import org.apache.derby.impl.jdbc.EmbedCallableStatement;
 
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.PreparedStatement;
 import java.sql.CallableStatement;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /* -- New jdbc 20 extension types --- */
 import javax.sql.ConnectionEventListener;
 import javax.sql.ConnectionEvent;
 
 /** 
 	A PooledConnection object is a connection object that provides hooks for
 	connection pool management.
 
 	<P>This is Derby's implementation of a PooledConnection for use in
 	the following environments:
 	<UL>
 	<LI> JDBC 3.0 - Java 2 - JDK 1.4, J2SE 5.0
 	<LI> JDBC 2.0 - Java 2 - JDK 1.2,1.3
 	</UL>
 
  */
 class EmbedPooledConnection implements javax.sql.PooledConnection, BrokeredConnectionControl
 {
     /** the connection string */
     private String connString;
 
     /**
      * The list of UPDATED commented {@code ConnectionEventListener}s. It is initially {@code
      * null} and will be initialized lazily when the first listener is added.
      */
     private ArrayList eventListener;

     
 }
