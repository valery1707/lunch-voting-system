CREATE TABLE test_entity_1 (
		id   UUID         NOT NULL CONSTRAINT pk$test_entity_1 PRIMARY KEY,
		name VARCHAR(255) NOT NULL,
);

CREATE TABLE test_entity_2 (
		id        UUID         NOT NULL CONSTRAINT pk$test_entity_2 PRIMARY KEY,
		parent_id UUID         NOT NULL CONSTRAINT fk$test_entity_2$parent_id REFERENCES test_entity_1,
		name      VARCHAR(255) NOT NULL,
);

CREATE TABLE test_entity_3 (
		id                UUID          NOT NULL CONSTRAINT pk$test_entity_3 PRIMARY KEY,
		parent_id         UUID          NOT NULL CONSTRAINT fk$test_entity_3$parent_id REFERENCES test_entity_2,
		name              VARCHAR(1024) NOT NULL,
		primitive_byte    TINYINT       NOT NULL,
		primitive_short   SMALLINT      NOT NULL,
		primitive_int     INT           NOT NULL,
		primitive_long    BIGINT        NOT NULL,
		primitive_float   REAL          NOT NULL,
		primitive_double  DOUBLE        NOT NULL,
		primitive_boolean BOOLEAN       NOT NULL,
		object_byte       TINYINT,
		object_short      SMALLINT,
		object_int        INT,
		object_long       BIGINT,
		object_float      REAL,
		object_double     DOUBLE,
		object_decimal    DECIMAL,
		object_boolean    BOOLEAN,
);

ALTER TABLE test_entity_1 ADD second_link_id UUID;
ALTER TABLE test_entity_1 ADD CONSTRAINT fk$test_entity_1$second_link_id FOREIGN KEY (second_link_id) REFERENCES test_entity_2;
ALTER TABLE test_entity_2 ADD third_link_id UUID;
ALTER TABLE test_entity_2 ADD CONSTRAINT fk$test_entity_2$third_link_id FOREIGN KEY (third_link_id) REFERENCES test_entity_3;

INSERT INTO test_entity_1 (id, name) VALUES ('25f641b9-4e96-4dfa-b932-43363f593721', '1');
INSERT INTO test_entity_2 (id, parent_id, name) VALUES ('61378eaa-f054-4bf2-9fe9-8b34dc70a627', '25f641b9-4e96-4dfa-b932-43363f593721', '1.1');
INSERT INTO test_entity_3 (id, parent_id, name, primitive_byte, primitive_short, primitive_int, primitive_long, primitive_float, primitive_double, primitive_boolean, object_byte, object_short, object_int, object_long, object_float, object_double, object_decimal, object_boolean)
VALUES
		('50c95060-a342-4d31-b8f5-590d66587d40', '61378eaa-f054-4bf2-9fe9-8b34dc70a627', '1.1.1', 1, 1, 1, 1, 1.1, 1.1, TRUE, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
		('dde67858-8741-47c6-b965-a2e3e216f00a', '61378eaa-f054-4bf2-9fe9-8b34dc70a627', '1.1.2', 2, 2, 2, 2, 2.2, 2.2, FALSE, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
		('7505fa25-4b54-40ff-86aa-389cf427eec8', '61378eaa-f054-4bf2-9fe9-8b34dc70a627', '1.1.3', 3, 3, 3, 3, 3.3, 3.3, TRUE, 3, 3, 3, 3, 3.3, 3.3, 3.3, TRUE),
		('ef45ed56-0b1c-46a0-9fce-98e84115c2b3', '61378eaa-f054-4bf2-9fe9-8b34dc70a627', '1.1.4', 4, 4, 4, 4, 4.4, 4.4, FALSE, 4, 4, 4, 4, 4.4, 4.4, 4.4, FALSE);
UPDATE test_entity_1 SET second_link_id = '61378eaa-f054-4bf2-9fe9-8b34dc70a627' WHERE name = '1';
UPDATE test_entity_2 SET third_link_id = '7505fa25-4b54-40ff-86aa-389cf427eec8' WHERE name = '1.1';
