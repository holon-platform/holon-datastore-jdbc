create table test1 (
     keycode numeric(20) 
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
    ,tm timestamp
    ,clb clob
    ,blb blob
    ,primary key (keycode)
);

create table test3 (
     code numeric(20) not null
    ,text varchar(100) not null
    ,primary key (code)
);