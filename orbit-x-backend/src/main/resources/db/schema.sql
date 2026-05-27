-- ============================================================
--  Orbit X Platform — Oracle Schema  (v1.0.0)
--  Usuário: rm559810 | Host: oracle.fiap.com.br:1521:ORCL
--
--  NOTA: Com ddl-auto=update o Hibernate gera as tabelas
--  automaticamente. Use este script apenas para referência
--  ou para criar manualmente no SQL*Plus / SQL Developer.
-- ============================================================

-- ── Sequences (Hibernate usa automaticamente com OracleDialect) ──
CREATE SEQUENCE companies_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE users_seq     START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE datacenters_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE satellites_seq  START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE alerts_seq      START WITH 1 INCREMENT BY 50;

-- ── Companies ─────────────────────────────────────────────────────
CREATE TABLE companies (
    id          NUMBER(19)    DEFAULT companies_seq.NEXTVAL PRIMARY KEY,
    name        VARCHAR2(100) NOT NULL,
    tax_id      VARCHAR2(20)  NOT NULL,
    admin_email VARCHAR2(150) NOT NULL,
    plan        VARCHAR2(20)  DEFAULT 'ENTERPRISE' NOT NULL
                              CHECK (plan IN ('STARTER','PROFESSIONAL','ENTERPRISE')),
    created_at  TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT uq_companies_tax_id UNIQUE (tax_id)
);

-- ── Users ──────────────────────────────────────────────────────────
CREATE TABLE users (
    id          NUMBER(19)    DEFAULT users_seq.NEXTVAL PRIMARY KEY,
    name        VARCHAR2(100) NOT NULL,
    email       VARCHAR2(150) NOT NULL,
    password    VARCHAR2(255) NOT NULL,
    role        VARCHAR2(20)  DEFAULT 'ADMIN' NOT NULL
                              CHECK (role IN ('ADMIN','OPERATOR','VIEWER')),
    company_id  NUMBER(19)    NOT NULL,
    created_at  TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
    last_login  TIMESTAMP,
    active      NUMBER(1)     DEFAULT 1 NOT NULL CHECK (active IN (0,1)),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_company FOREIGN KEY (company_id)
        REFERENCES companies(id) ON DELETE CASCADE
);

CREATE INDEX idx_users_email   ON users (email);
CREATE INDEX idx_users_company ON users (company_id);

-- ── Datacenters ────────────────────────────────────────────────────
CREATE TABLE datacenters (
    id                       NUMBER(19)       DEFAULT datacenters_seq.NEXTVAL PRIMARY KEY,
    name                     VARCHAR2(150)    NOT NULL,
    city                     VARCHAR2(100)    NOT NULL,
    country                  VARCHAR2(100)    NOT NULL,
    latitude                 BINARY_DOUBLE    NOT NULL,
    longitude                BINARY_DOUBLE    NOT NULL,
    thermal_state            VARCHAR2(20)     DEFAULT 'STABLE' NOT NULL
                                              CHECK (thermal_state IN ('OPTIMAL','STABLE','CRITICAL')),
    regional_consumption_kwh NUMBER(12,2),
    capacity_servers         NUMBER(10),
    active                   NUMBER(1)        DEFAULT 1 NOT NULL CHECK (active IN (0,1)),
    created_at               TIMESTAMP        DEFAULT SYSTIMESTAMP NOT NULL
);

-- ── Satellites ─────────────────────────────────────────────────────
CREATE TABLE satellites (
    id                  NUMBER(19)       DEFAULT satellites_seq.NEXTVAL PRIMARY KEY,
    name                VARCHAR2(100)    NOT NULL,
    orbit_type          VARCHAR2(10)     NOT NULL CHECK (orbit_type IN ('LEO','MEO','GEO','HEO')),
    altitude_km         BINARY_DOUBLE    NOT NULL,
    inclination_deg     BINARY_DOUBLE    NOT NULL,
    orbital_period_min  BINARY_DOUBLE    NOT NULL,
    data_link_status    VARCHAR2(20)     DEFAULT 'ACTIVE' NOT NULL
                                         CHECK (data_link_status IN ('ACTIVE','DEGRADED','OFFLINE','MAINTENANCE')),
    active              NUMBER(1)        DEFAULT 1 NOT NULL CHECK (active IN (0,1)),
    launched_at         TIMESTAMP,
    CONSTRAINT uq_satellites_name UNIQUE (name)
);

-- ── Alerts ─────────────────────────────────────────────────────────
CREATE TABLE alerts (
    id               NUMBER(19)    DEFAULT alerts_seq.NEXTVAL PRIMARY KEY,
    title            VARCHAR2(200) NOT NULL,
    message          VARCHAR2(4000) NOT NULL,
    severity         VARCHAR2(20)  NOT NULL
                                   CHECK (severity IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    source_component VARCHAR2(100),
    datacenter_id    NUMBER(19)    REFERENCES datacenters(id) ON DELETE SET NULL,
    resolved         NUMBER(1)     DEFAULT 0 NOT NULL CHECK (resolved IN (0,1)),
    created_at       TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
    resolved_at      TIMESTAMP
);

CREATE INDEX idx_alerts_resolved   ON alerts (resolved, created_at DESC);
CREATE INDEX idx_alerts_datacenter ON alerts (datacenter_id);
CREATE INDEX idx_alerts_severity   ON alerts (severity) WHERE resolved = 0; -- parcial (Oracle 11g+)
