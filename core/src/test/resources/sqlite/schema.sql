drop table if exists test1;
drop table if exists test2;
drop table if exists test3;

create table test1 (
	 keycode bigint primary key 
	,strv varchar(100) 
	,decv double 
	,datv date 
	,datv2 date 
	,enmv int 
	,nbv int not null
	,nst1 varchar(10) 
	,nst2 numeric(19,2) 
	,nss1 varchar(10) 
	,tms timestamp
	,tms2 timestamp
	,tm time
	,clb clob
	,blb blob
);

create table test2 (
	 code INTEGER primary key 
	,text varchar(100) not null
);

create table test3 (
	 code bigint not null primary key
	,text varchar(100) not null
);

