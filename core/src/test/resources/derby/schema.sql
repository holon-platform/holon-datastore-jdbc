create table test1 (
	 keycode bigint primary key 
	,strv varchar(100) 
	,decv double 
	,datv date 
	,datv2 date 
	,enmv int 
	,nbv int not null
	,nst1 varchar(10) default 'nst1'
	,nst2 decimal(14,4) 
	,nss1 varchar(10) 
	,tms timestamp
	,tms2 timestamp
	,tm time
	,clb clob
	,blb blob
);

create table test2 (
	 code bigint NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)
	,text varchar(100) not null
	,CONSTRAINT primary_key PRIMARY KEY (code)
);

create table test3 (
	 code bigint not null primary key
	,text varchar(100) not null
);

create table test_recur (
	 code bigint NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)
	,name varchar(100) not null
	,parent varchar(100)
	,CONSTRAINT r_primary_key PRIMARY KEY (code)
);

create table test_nopk (
	nmb numeric(10),
	txt varchar(10)
)
