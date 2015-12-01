CREATE TABLE incident (
  id      BIGINT      NOT NULL PRIMARY KEY AUTO_INCREMENT,
  topic   TEXT        NOT NULL,
  channel VARCHAR(64) NOT NULL,
  created BIGINT      NOT NULL,
);

CREATE TABLE status (
  id          BIGINT      NOT NULL PRIMARY KEY AUTO_INCREMENT,
  incident_id BIGINT      NOT NULL,
  message     TEXT        NOT NULL,
  creator     VARCHAR(64) NOT NULL,
  created     BIGINT      NOT NULL,
  CONSTRAINT fk_status_incident_id FOREIGN KEY (incident_id) REFERENCES incident (id),
);