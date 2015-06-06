#!/bin/bash

VERSION="3.2.5.RELEASE"
NAME=spring-aop
cat >${NAME}.bnd  <<STARTBND
Bundle-Name                             ${NAME}
Bundle-SymbolicName                     ${NAME}
Bundle-Version                          ${VERSION}
Export-Package                          org.springframework.aop;version="${VERSION}";uses:="org.aopalliance.aop,org.aopalliance.intercept,org.springframework.core",org.springframework.aop.aspectj;version="${VERSION}";uses:="org.aopalliance.aop,org.aopalliance.intercept,org.aspectj.bridge,org.aspectj.lang,org.aspectj.lang.reflect,org.aspectj.runtime.internal,org.aspectj.weaver.ast,org.aspectj.weaver.internal.tools,org.aspectj.weaver.reflect,org.aspectj.weaver.tools,org.springframework.aop,org.springframework.aop.support,org.springframework.beans.factory,org.springframework.core",org.springframework.aop.aspectj.annotation;version="${VERSION}";uses:="org.aopalliance.aop,org.aspectj.lang.reflect,org.springframework.aop,org.springframework.aop.aspectj,org.springframework.aop.aspectj.autoproxy,org.springframework.aop.framework,org.springframework.aop.support,org.springframework.beans.factory,org.springframework.beans.factory.config,org.springframework.core,org.springframework.util",org.springframework.aop.aspectj.autoproxy;version="${VERSION}";uses:="org.aspectj.util,org.springframework.aop,org.springframework.aop.framework.autoproxy",org.springframework.aop.config;version="${VERSION}";uses:="org.springframework.aop.aspectj,org.springframework.beans.factory,org.springframework.beans.factory.config,org.springframework.beans.factory.parsing,org.springframework.beans.factory.support,org.springframework.beans.factory.xml,org.w3c.dom",org.springframework.aop.framework;version="${VERSION}";uses:="net.sf.cglib.proxy,org.aopalliance.aop,org.aopalliance.intercept,org.springframework.aop,org.springframework.aop.framework.adapter,org.springframework.beans,org.springframework.beans.factory,org.springframework.core",org.springframework.aop.framework.adapter;version="${VERSION}";uses:="org.aopalliance.aop,org.aopalliance.intercept,org.springframework.aop,org.springframework.beans,org.springframework.beans.factory.config",org.springframework.aop.framework.autoproxy;version="${VERSION}";uses:="org.springframework.aop,org.springframework.aop.framework,org.springframework.aop.framework.adapter,org.springframework.beans,org.springframework.beans.factory,org.springframework.beans.factory.config,org.springframework.core",org.springframework.aop.framework.autoproxy.target;version="${VERSION}";uses:="org.springframework.aop,org.springframework.aop.framework.autoproxy,org.springframework.aop.target,org.springframework.beans.factory,org.springframework.beans.factory.config,org.springframework.beans.factory.support",org.springframework.aop.interceptor;version="${VERSION}";uses:="org.aopalliance.aop,org.aopalliance.intercept,org.apache.commons.logging,org.springframework.aop,org.springframework.aop.support,org.springframework.beans,org.springframework.beans.factory,org.springframework.core,org.springframework.core.task,org.springframework.util",org.springframework.aop.scope;version="${VERSION}";uses:="org.springframework.aop,org.springframework.aop.framework,org.springframework.beans.factory,org.springframework.beans.factory.config,org.springframework.beans.factory.support",org.springframework.aop.support;version="${VERSION}";uses:="org.aopalliance.aop,org.aopalliance.intercept,org.springframework.aop,org.springframework.beans.factory,org.springframework.core",org.springframework.aop.support.annotation;version="${VERSION}";uses:="org.springframework.aop,org.springframework.aop.support",org.springframework.aop.target;version="${VERSION}";uses:="org.apache.commons.pool,org.springframework.aop,org.springframework.aop.support,org.springframework.beans,org.springframework.beans.factory",org.springframework.aop.target.dynamic;version="${VERSION}";uses:="org.springframework.aop,org.springframework.beans.factory"
Import-Package                          com.jamonapi;version="[2.4.0, 3.0.0)";resolution:="optional",net.sf.cglib.core;version="[2.1.3, 3.0.0)";resolution:="optional",net.sf.cglib.proxy;version="[2.1.3, 3.0.0)";resolution:="optional",net.sf.cglib.transform.impl;version="[2.1.3, 3.0.0)";resolution:="optional",org.aopalliance.aop;version="[1.0.0, 2.0.0)",org.aopalliance.intercept;version="[1.0.0, 2.0.0)",org.apache.commons.logging;version="[1.1.1, 2.0.0)",org.apache.commons.pool;version="[1.3.0, 2.0.0)";resolution:="optional",org.apache.commons.pool.impl;version="[1.3.0, 2.0.0)";resolution:="optional",org.aspectj.bridge;version="[1.6.8, 2.0.0)";resolution:="optional",org.aspectj.lang;version="[1.6.8, 2.0.0)";resolution:="optional",org.aspectj.lang.annotation;version="[1.6.8, 2.0.0)";resolution:="optional",org.aspectj.lang.reflect;version="[1.6.8, 2.0.0)";resolution:="optional",org.aspectj.runtime.internal;version="[1.6.8, 2.0.0)";resolution:="optional",org.aspectj.util;version="[1.6.8, 2.0.0)";resolution:="optional",org.aspectj.weaver;version="[1.6.8, 2.0.0)";resolution:="optional",org.aspectj.weaver.ast;version="[1.6.8, 2.0.0)";resolution:="optional",org.aspectj.weaver.internal.tools;version="[1.6.8, 2.0.0)";resolution:="optional",org.aspectj.weaver.patterns;version="[1.6.8, 2.0.0)";resolution:="optional",org.aspectj.weaver.reflect;version="[1.6.8, 2.0.0)";resolution:="optional",org.aspectj.weaver.tools;version="[1.6.8, 2.0.0)";resolution:="optional",org.springframework.beans;version="[3.1.3,3.2.6)";resolution:="optional",org.springframework.beans.factory;version="[3.1.3,3.2.6)";resolution:="optional",org.springframework.beans.factory.annotation;version="[3.1.3,3.2.6)";resolution:="optional",org.springframework.beans.factory.config;version="[3.1.3,3.2.6)";resolution:="optional",org.springframework.beans.factory.parsing;version="[3.1.3,3.2.6)";resolution:="optional",org.springframework.beans.factory.support;version="[3.1.3,3.2.6)";resolution:="optional",org.springframework.beans.factory.xml;version="[3.1.3,3.2.6)";resolution:="optional",org.springframework.core;version="[3.1.3,3.2.6)",org.springframework.core.annotation;version="[3.1.3,3.2.6)",org.springframework.core.task;version="[3.1.3,3.2.6)",org.springframework.core.task.support;version="[3.1.3,3.2.6)",org.springframework.util;version="[3.1.3,3.2.6)",org.springframework.util.xml;version="[3.1.3,3.2.6)",org.w3c.dom;version="0";resolution:="optional"
-noee=true 
STARTBND

bnd wrap -o ${NAME}-${VERSION}.bar -p ${NAME}.bnd ${NAME}-${VERSION}.jar


rm -f ${NAME}.bnd