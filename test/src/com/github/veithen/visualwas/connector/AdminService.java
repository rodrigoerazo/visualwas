package com.github.veithen.visualwas.connector;

import javax.management.JMException;
import javax.management.ObjectName;

public interface AdminService {
    ObjectName getServerMBean() throws JMException;
    
    Object invoke(@Param(name="objectname") ObjectName objectName,
                  @Param(name="operationname") String operationName,
                  @Param(name="params") Object[] params,
                  @Param(name="signature") String[] signature) throws JMException;
}