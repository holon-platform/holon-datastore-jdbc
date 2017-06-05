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
	,tms timestamp NULL
	,tms2 timestamp NULL
	,tm time
	,clb longtext
	,blb longblob
);

create table test2 (
	 code bigint primary key auto_increment
	,text varchar(100) not null
);

create table test3 (
	 code bigint not null primary key
	,text varchar(100) not null
);
