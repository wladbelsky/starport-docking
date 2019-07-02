create table USERS
(
  EMAIL    VARCHAR2(32) not null,
  PASSWORD VARCHAR2(64) not null,
  TOKEN    VARCHAR2(64),
  AVATAR   LONG
)
/

create unique index USERS_NICKNAME_UINDEX
  on USERS (NICKNAME)
/

alter table USERS
  add constraint USERS_PK
    primary key (EMAIL)
/

create table SIZES
(
  "SIZE" VARCHAR2(1) not null
)
/

create unique index SIZES_SIZE_UINDEX
  on SIZES ("SIZE")
/

alter table SIZES
  add constraint SIZES_PK
    primary key ("SIZE")
/

create table LOCATIONS
(
  ID   NUMBER generated as identity (minvalue 0),
  NAME VARCHAR2(64) not null
)
/

create unique index LOCATIONS_ID_UINDEX
  on LOCATIONS (ID)
/

alter table LOCATIONS
  add constraint LOCATIONS_PK
    primary key (ID)
/

create table LANDINGPADS
(
  ID          NUMBER generated as identity
    constraint LANDINGPADS_PK
      primary key,
  "SIZE"      VARCHAR2(1) not null
    constraint LANDINGPADS_SIZES_SIZE_FK
      references SIZES,
  PAD_NUM     NUMBER,
  LOCATION_ID NUMBER
    constraint LANDINGPADS_LOCATIONS_ID_FK
      references LOCATIONS
)
/

create table REQUESTS
(
  ID         NUMBER generated as identity (minvalue 0)
    constraint REQUESTS_PK
      primary key,
  PAD_ID     NUMBER not null
    constraint REQUESTS_LANDINGPADS_ID_FK
      references LANDINGPADS,
  "USER"     VARCHAR2(32)
    constraint REQUESTS_USERS_NICKNAME_FK
      references USERS,
  DATE_START DATE   not null,
  DATE_END   DATE   not null
)
/


