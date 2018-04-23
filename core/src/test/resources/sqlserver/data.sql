INSERT INTO test1 VALUES (1, 'One', 7.4, '20160519', '20160519', 0, 1, 'n1', 12.65, 's1', null, null, '18:30:15', 'clocbcontent', 0xC9CBBBCCCEB9C8CABCCCCEB9C9CBBB);
INSERT INTO test1 VALUES (2, 'Two', null, '20160419', '20160419', 1, 0, 'n2', 3, 's2', '20170323 15:30:25', '20170323 15:30:25', null, 'clocbcontent', null);

INSERT INTO test3 VALUES (2, 'TestJoin');
INSERT INTO test3 VALUES (3, 'TestJoin3');

INSERT INTO test_recur (name, parent) VALUES ('test1', null);
INSERT INTO test_recur (name, parent) VALUES ('test2', 'test1');
INSERT INTO test_recur (name, parent) VALUES ('test3', 'test2');

INSERT INTO test_nopk (nmb, txt) VALUES (1, 'First');
INSERT INTO test_nopk (nmb, txt) VALUES (2, 'Second');

COMMIT;