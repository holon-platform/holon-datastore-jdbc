create table test1 (
	 keycode bigint primary key 
	,strv varchar(100) 
	,decv numeric(16,6) 
	,datv date 
	,datv2 date 
	,enmv int 
	,nbv int not null
	,nst1 varchar(10) default 'nst1'
	,nst2 numeric(14,4) 
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

create table test_recur (
	 code SERIAL primary key
	,name varchar(100) not null
	,parent varchar(100)
);

create table test_nopk (
	nmb numeric(10),
	txt varchar(10)
)
