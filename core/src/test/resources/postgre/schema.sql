create table test1 (
	 keycode bigint primary key 
	,strv varchar(100) 
	,decv numeric(16,6) 
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
	,clb TEXT
	,blb BYTEA
);

create table test2 (
	 code SERIAL primary key
	,text varchar(100) not null
);

create table test3 (
	 code bigint not null primary key
	,text varchar(100) not null
);
