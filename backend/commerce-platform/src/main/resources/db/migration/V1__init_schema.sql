-- ============================================================
-- V1__init_schema.sql
-- AI Commerce Platform - Complete schema from Hibernate
-- ============================================================

-- PostgreSQL database dump
CREATE TABLE cart (
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    user_id bigint NOT NULL
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE cart_item (
    price numeric(10,2) NOT NULL,
    quantity integer NOT NULL,
    selected boolean NOT NULL,
    cart_id bigint NOT NULL,
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    product_id bigint NOT NULL,
    sku_id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    status character varying(20) NOT NULL,
    product_name character varying(200),
    product_image character varying(500),
    CONSTRAINT cart_item_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'REMOVED'::character varying, 'CHECKED_OUT'::character varying])::text[])))
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE category (
    deleted boolean NOT NULL,
    level integer NOT NULL,
    sort integer NOT NULL,
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    parent_id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    category_name character varying(64) NOT NULL
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE checkout_transaction (
    cart_id bigint NOT NULL,
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    user_id bigint NOT NULL,
    status character varying(20) NOT NULL,
    checkout_no character varying(64) NOT NULL,
    order_no character varying(64),
    fail_reason character varying(500),
    CONSTRAINT checkout_transaction_status_check CHECK (((status)::text = ANY ((ARRAY['INIT'::character varying, 'PROCESSING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying])::text[])))
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE fulfillment (
    created_time timestamp(6) without time zone,
    estimated_arrival timestamp(6) without time zone,
    id bigint NOT NULL,
    merchant_id bigint NOT NULL,
    order_id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    warehouse_id bigint,
    status character varying(30) NOT NULL,
    carrier_code character varying(50),
    carrier character varying(100),
    tracking_number character varying(100),
    shipping_address character varying(500),
    CONSTRAINT fulfillment_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PROCESSING'::character varying, 'PICKING'::character varying, 'PACKING'::character varying, 'WAITING_SHIPMENT'::character varying, 'SHIPPED'::character varying, 'DELIVERED'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying, 'CANCELLED'::character varying])::text[])))
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE inventory (
    available_stock integer NOT NULL,
    locked_stock integer NOT NULL,
    reserved_stock integer NOT NULL,
    sold_stock integer NOT NULL,
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    product_id bigint NOT NULL,
    sku_id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    status character varying(20) NOT NULL,
    CONSTRAINT inventory_status_check CHECK (((status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'LOCKED'::character varying, 'DEDUCTED'::character varying, 'RELEASED'::character varying])::text[])))
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE inventory_movement (
    after_available integer NOT NULL,
    after_reserved integer NOT NULL,
    before_available integer NOT NULL,
    before_reserved integer NOT NULL,
    quantity integer NOT NULL,
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    inventory_id bigint NOT NULL,
    operator_id bigint,
    product_sku_id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    movement_type character varying(20) NOT NULL,
    source_type character varying(20),
    reason_code character varying(30),
    business_id character varying(64),
    movement_no character varying(64) NOT NULL,
    operator_name character varying(64),
    source_id character varying(64),
    remark character varying(256),
    CONSTRAINT inventory_movement_movement_type_check CHECK (((movement_type)::text = ANY ((ARRAY['INBOUND'::character varying, 'OUTBOUND'::character varying, 'RESERVE'::character varying, 'RELEASE'::character varying, 'DEDUCT'::character varying, 'ADJUST'::character varying, 'RETURN'::character varying, 'DAMAGE'::character varying])::text[]))),
    CONSTRAINT inventory_movement_reason_code_check CHECK (((reason_code)::text = ANY ((ARRAY['NORMAL_INBOUND'::character varying, 'MANUAL_ADJUST'::character varying, 'ORDER_RESERVE'::character varying, 'ORDER_RELEASE'::character varying, 'ORDER_DEDUCT'::character varying, 'RETURN'::character varying, 'DAMAGE'::character varying, 'SYSTEM_SYNC'::character varying])::text[]))),
    CONSTRAINT inventory_movement_source_type_check CHECK (((source_type)::text = ANY ((ARRAY['MERCHANT'::character varying, 'ORDER'::character varying, 'ADMIN'::character varying, 'SYSTEM'::character varying])::text[])))
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE inventory_reservation (
    quantity integer NOT NULL,
    created_time timestamp(6) without time zone,
    expire_time timestamp(6) without time zone NOT NULL,
    id bigint NOT NULL,
    inventory_id bigint NOT NULL,
    order_id bigint NOT NULL,
    product_sku_id bigint NOT NULL,
    sku_id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    status character varying(20) NOT NULL,
    order_no character varying(32) NOT NULL,
    reservation_no character varying(64) NOT NULL,
    CONSTRAINT inventory_reservation_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'DEDUCTED'::character varying, 'RELEASED'::character varying, 'EXPIRED'::character varying])::text[])))
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE orders (
    deleted boolean NOT NULL,
    discount_amount numeric(18,2) NOT NULL,
    freight_amount numeric(18,2) NOT NULL,
    pay_amount numeric(18,2) NOT NULL,
    product_amount numeric(18,2) NOT NULL,
    total_amount numeric(18,2) NOT NULL,
    buyer_id bigint NOT NULL,
    cancelled_time timestamp(6) without time zone,
    completed_time timestamp(6) without time zone,
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    merchant_id bigint NOT NULL,
    payment_time timestamp(6) without time zone,
    shipping_time timestamp(6) without time zone,
    store_id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    version bigint NOT NULL,
    order_status character varying(20) NOT NULL,
    payment_status character varying(20) NOT NULL,
    shipping_status character varying(20) NOT NULL,
    order_no character varying(32) NOT NULL,
    buyer_remark character varying(500),
    merchant_remark character varying(500),
    CONSTRAINT orders_order_status_check CHECK (((order_status)::text = ANY ((ARRAY['PENDING_PAYMENT'::character varying, 'PAID'::character varying, 'PROCESSING'::character varying, 'SHIPPED'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying, 'REFUNDING'::character varying, 'REFUNDED'::character varying, 'CLOSED'::character varying])::text[]))),
    CONSTRAINT orders_payment_status_check CHECK (((payment_status)::text = ANY ((ARRAY['UNPAID'::character varying, 'PAID'::character varying, 'REFUNDING'::character varying, 'REFUNDED'::character varying])::text[]))),
    CONSTRAINT orders_shipping_status_check CHECK (((shipping_status)::text = ANY ((ARRAY['UNSHIPPED'::character varying, 'PART_SHIPPED'::character varying, 'SHIPPED'::character varying, 'RECEIVED'::character varying])::text[])))
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE outbox_event (
    retry_count integer,
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    processed_time timestamp(6) without time zone,
    status character varying(20) NOT NULL,
    event_id character varying(64) NOT NULL,
    aggregate_id character varying(100),
    aggregate_type character varying(100),
    event_type character varying(255) NOT NULL,
    payload text NOT NULL,
    CONSTRAINT outbox_event_status_check CHECK (((status)::text = ANY ((ARRAY['NEW'::character varying, 'PROCESSING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying])::text[])))
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE packing_task (
    created_time timestamp(6) without time zone,
    fulfillment_id bigint NOT NULL,
    id bigint NOT NULL,
    packed_at timestamp(6) without time zone,
    picking_task_id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    status character varying(20) NOT NULL,
    CONSTRAINT packing_task_status_check CHECK (((status)::text = ANY ((ARRAY['CREATED'::character varying, 'PROCESSING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying, 'CANCELLED'::character varying])::text[])))
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE payment (
    amount numeric(19,2) NOT NULL,
    created_at timestamp(6) without time zone,
    created_time timestamp(6) without time zone,
    failed_at timestamp(6) without time zone,
    id bigint NOT NULL,
    order_id bigint NOT NULL,
    paid_at timestamp(6) without time zone,
    updated_time timestamp(6) without time zone,
    user_id bigint NOT NULL,
    status character varying(30) NOT NULL,
    payment_no character varying(64) NOT NULL,
    transaction_no character varying(128),
    CONSTRAINT payment_status_check CHECK (((status)::text = ANY ((ARRAY['CREATED'::character varying, 'PROCESSING'::character varying, 'PAID'::character varying, 'CANCELLED'::character varying, 'FAILED'::character varying])::text[])))
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE permissions (
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    code character varying(100) NOT NULL,
    name character varying(100) NOT NULL,
    description character varying(255)
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE picking_task (
    completed_at timestamp(6) without time zone,
    created_time timestamp(6) without time zone,
    fulfillment_id bigint NOT NULL,
    id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    warehouse_id bigint NOT NULL,
    status character varying(20) NOT NULL,
    CONSTRAINT picking_task_status_check CHECK (((status)::text = ANY ((ARRAY['CREATED'::character varying, 'PROCESSING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying, 'CANCELLED'::character varying])::text[])))
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE processed_event (
    id bigint NOT NULL,
    processed_time timestamp(6) without time zone,
    event_id character varying(64) NOT NULL,
    consumer_name character varying(255) NOT NULL
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE product (
    deleted boolean NOT NULL,
    sales_count integer NOT NULL,
    category_id bigint NOT NULL,
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    merchant_id bigint NOT NULL,
    store_id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    version bigint NOT NULL,
    status character varying(20) NOT NULL,
    brand character varying(64),
    product_code character varying(64) NOT NULL,
    product_name character varying(256) NOT NULL,
    description text,
    CONSTRAINT product_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PENDING_REVIEW'::character varying, 'REJECTED'::character varying, 'ON_SHELF'::character varying, 'OFF_SHELF'::character varying, 'ARCHIVED'::character varying])::text[])))
);
CREATE TABLE product_audit_record (
    created_time timestamp(6) without time zone NOT NULL,
    id bigint NOT NULL,
    product_id bigint NOT NULL,
    reviewer_id bigint NOT NULL,
    action character varying(20) NOT NULL,
    after_status character varying(20) NOT NULL,
    before_status character varying(20) NOT NULL,
    audit_remark character varying(500)
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE product_image (
    deleted boolean NOT NULL,
    is_cover boolean NOT NULL,
    sort integer NOT NULL,
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    product_id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    image_type character varying(20) NOT NULL,
    url character varying(512) NOT NULL,
    CONSTRAINT product_image_image_type_check CHECK (((image_type)::text = ANY ((ARRAY['MAIN'::character varying, 'DETAIL'::character varying, 'SKU'::character varying])::text[])))
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE product_sku (
    deleted boolean NOT NULL,
    original_price numeric(12,2),
    price numeric(12,2) NOT NULL,
    sales_count integer NOT NULL,
    weight numeric(10,3),
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    product_id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    status character varying(20) NOT NULL,
    sku_code character varying(64) NOT NULL,
    attributes_json json NOT NULL
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE product_spec (
    deleted boolean NOT NULL,
    sort integer NOT NULL,
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    product_id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    spec_name character varying(64) NOT NULL,
    spec_values json NOT NULL
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE refund (
    amount numeric(18,2) NOT NULL,
    completed_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    id bigint NOT NULL,
    order_id bigint NOT NULL,
    user_id bigint NOT NULL,
    status character varying(20) NOT NULL,
    reason character varying(30) NOT NULL,
    CONSTRAINT refund_reason_check CHECK (((reason)::text = ANY ((ARRAY['QUALITY_ISSUE'::character varying, 'WRONG_PRODUCT'::character varying, 'CUSTOMER_CHANGE_MIND'::character varying, 'DAMAGED'::character varying, 'OTHER'::character varying])::text[]))),
    CONSTRAINT refund_status_check CHECK (((status)::text = ANY ((ARRAY['REQUESTED'::character varying, 'APPROVED'::character varying, 'PROCESSING'::character varying, 'COMPLETED'::character varying, 'REJECTED'::character varying, 'FAILED'::character varying])::text[])))
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE roles (
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    name character varying(50) NOT NULL,
    description character varying(255)
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE shipment (
    created_time timestamp(6) without time zone,
    delivered_at timestamp(6) without time zone,
    fulfillment_id bigint NOT NULL,
    id bigint NOT NULL,
    packing_task_id bigint NOT NULL,
    shipped_at timestamp(6) without time zone,
    updated_time timestamp(6) without time zone,
    delivery_status character varying(20),
    status character varying(20) NOT NULL,
    carrier character varying(100),
    tracking_number character varying(100),
    CONSTRAINT shipment_delivery_status_check CHECK (((delivery_status)::text = ANY ((ARRAY['WAITING'::character varying, 'OUT_FOR_DELIVERY'::character varying, 'DELIVERED'::character varying, 'FAILED'::character varying])::text[]))),
    CONSTRAINT shipment_status_check CHECK (((status)::text = ANY ((ARRAY['CREATED'::character varying, 'READY_TO_SHIP'::character varying, 'SHIPPED'::character varying, 'IN_TRANSIT'::character varying, 'DELIVERED'::character varying, 'CANCELLED'::character varying, 'FAILED'::character varying])::text[])))
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE stock_reservation (
    quantity integer NOT NULL,
    confirmed_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone,
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    order_id bigint NOT NULL,
    product_id bigint NOT NULL,
    released_at timestamp(6) without time zone,
    updated_time timestamp(6) without time zone,
    status character varying(30) NOT NULL,
    CONSTRAINT stock_reservation_status_check CHECK (((status)::text = ANY ((ARRAY['RESERVED'::character varying, 'CONFIRMED'::character varying, 'RELEASED'::character varying, 'FAILED'::character varying])::text[])))
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE tracking_record (
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    occurred_at timestamp(6) without time zone,
    shipment_id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    status character varying(50),
    location character varying(200),
    description character varying(500)
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE users (
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    phone character varying(20),
    role character varying(20) NOT NULL,
    status character varying(20) NOT NULL,
    username character varying(50) NOT NULL,
    email character varying(100) NOT NULL,
    nickname character varying(100),
    avatar character varying(500),
    password_hash character varying(255) NOT NULL,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['CUSTOMER'::character varying, 'MERCHANT'::character varying, 'ADMIN'::character varying, 'SUPER_ADMIN'::character varying])::text[]))),
    CONSTRAINT users_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'LOCKED'::character varying])::text[])))
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE warehouse (
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    status character varying(20) NOT NULL,
    code character varying(50) NOT NULL,
    name character varying(200) NOT NULL,
    address character varying(500),
    CONSTRAINT warehouse_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'MAINTENANCE'::character varying])::text[])))
);
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE ONLY cart ALTER COLUMN id SET DEFAULT nextval('cart_id_seq'::regclass);
ALTER TABLE ONLY cart_item ALTER COLUMN id SET DEFAULT nextval('cart_item_id_seq'::regclass);
ALTER TABLE ONLY category ALTER COLUMN id SET DEFAULT nextval('category_id_seq'::regclass);
ALTER TABLE ONLY checkout_transaction ALTER COLUMN id SET DEFAULT nextval('checkout_transaction_id_seq'::regclass);
ALTER TABLE ONLY fulfillment ALTER COLUMN id SET DEFAULT nextval('fulfillment_id_seq'::regclass);
ALTER TABLE ONLY inventory ALTER COLUMN id SET DEFAULT nextval('inventory_id_seq'::regclass);
ALTER TABLE ONLY inventory_movement ALTER COLUMN id SET DEFAULT nextval('inventory_movement_id_seq'::regclass);
ALTER TABLE ONLY inventory_reservation ALTER COLUMN id SET DEFAULT nextval('inventory_reservation_id_seq'::regclass);
ALTER TABLE ONLY orders ALTER COLUMN id SET DEFAULT nextval('orders_id_seq'::regclass);
ALTER TABLE ONLY outbox_event ALTER COLUMN id SET DEFAULT nextval('outbox_event_id_seq'::regclass);
ALTER TABLE ONLY packing_task ALTER COLUMN id SET DEFAULT nextval('packing_task_id_seq'::regclass);
ALTER TABLE ONLY payment ALTER COLUMN id SET DEFAULT nextval('payment_id_seq'::regclass);
ALTER TABLE ONLY permissions ALTER COLUMN id SET DEFAULT nextval('permissions_id_seq'::regclass);
ALTER TABLE ONLY picking_task ALTER COLUMN id SET DEFAULT nextval('picking_task_id_seq'::regclass);
ALTER TABLE ONLY processed_event ALTER COLUMN id SET DEFAULT nextval('processed_event_id_seq'::regclass);
ALTER TABLE ONLY product ALTER COLUMN id SET DEFAULT nextval('product_id_seq'::regclass);
ALTER TABLE ONLY product_audit_record ALTER COLUMN id SET DEFAULT nextval('product_audit_record_id_seq'::regclass);
ALTER TABLE ONLY product_image ALTER COLUMN id SET DEFAULT nextval('product_image_id_seq'::regclass);
ALTER TABLE ONLY product_sku ALTER COLUMN id SET DEFAULT nextval('product_sku_id_seq'::regclass);
ALTER TABLE ONLY product_spec ALTER COLUMN id SET DEFAULT nextval('product_spec_id_seq'::regclass);
ALTER TABLE ONLY refund ALTER COLUMN id SET DEFAULT nextval('refund_id_seq'::regclass);
ALTER TABLE ONLY roles ALTER COLUMN id SET DEFAULT nextval('roles_id_seq'::regclass);
ALTER TABLE ONLY shipment ALTER COLUMN id SET DEFAULT nextval('shipment_id_seq'::regclass);
ALTER TABLE ONLY stock_reservation ALTER COLUMN id SET DEFAULT nextval('stock_reservation_id_seq'::regclass);
ALTER TABLE ONLY tracking_record ALTER COLUMN id SET DEFAULT nextval('tracking_record_id_seq'::regclass);
ALTER TABLE ONLY users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);
ALTER TABLE ONLY warehouse ALTER COLUMN id SET DEFAULT nextval('warehouse_id_seq'::regclass);
ALTER TABLE ONLY cart_item
    ADD CONSTRAINT cart_item_pkey PRIMARY KEY (id);
ALTER TABLE ONLY cart
    ADD CONSTRAINT cart_pkey PRIMARY KEY (id);
ALTER TABLE ONLY category
    ADD CONSTRAINT category_pkey PRIMARY KEY (id);
ALTER TABLE ONLY checkout_transaction
    ADD CONSTRAINT checkout_transaction_checkout_no_key UNIQUE (checkout_no);
ALTER TABLE ONLY checkout_transaction
    ADD CONSTRAINT checkout_transaction_pkey PRIMARY KEY (id);
ALTER TABLE ONLY fulfillment
    ADD CONSTRAINT fulfillment_order_id_key UNIQUE (order_id);
ALTER TABLE ONLY fulfillment
    ADD CONSTRAINT fulfillment_pkey PRIMARY KEY (id);
ALTER TABLE ONLY inventory_movement
    ADD CONSTRAINT inventory_movement_movement_no_key UNIQUE (movement_no);
ALTER TABLE ONLY inventory_movement
    ADD CONSTRAINT inventory_movement_pkey PRIMARY KEY (id);
ALTER TABLE ONLY inventory
    ADD CONSTRAINT inventory_pkey PRIMARY KEY (id);
ALTER TABLE ONLY inventory_reservation
    ADD CONSTRAINT inventory_reservation_pkey PRIMARY KEY (id);
ALTER TABLE ONLY inventory_reservation
    ADD CONSTRAINT inventory_reservation_reservation_no_key UNIQUE (reservation_no);
ALTER TABLE ONLY inventory
    ADD CONSTRAINT inventory_sku_id_key UNIQUE (sku_id);
ALTER TABLE ONLY orders
    ADD CONSTRAINT orders_order_no_key UNIQUE (order_no);
ALTER TABLE ONLY orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);
ALTER TABLE ONLY outbox_event
    ADD CONSTRAINT outbox_event_event_id_key UNIQUE (event_id);
ALTER TABLE ONLY outbox_event
    ADD CONSTRAINT outbox_event_pkey PRIMARY KEY (id);
ALTER TABLE ONLY packing_task
    ADD CONSTRAINT packing_task_pkey PRIMARY KEY (id);
ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_payment_no_key UNIQUE (payment_no);
ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_pkey PRIMARY KEY (id);
ALTER TABLE ONLY permissions
    ADD CONSTRAINT permissions_code_key UNIQUE (code);
ALTER TABLE ONLY permissions
    ADD CONSTRAINT permissions_pkey PRIMARY KEY (id);
ALTER TABLE ONLY picking_task
    ADD CONSTRAINT picking_task_pkey PRIMARY KEY (id);
ALTER TABLE ONLY processed_event
    ADD CONSTRAINT processed_event_pkey PRIMARY KEY (id);
ALTER TABLE ONLY product_audit_record
    ADD CONSTRAINT product_audit_record_pkey PRIMARY KEY (id);
ALTER TABLE ONLY product_image
    ADD CONSTRAINT product_image_pkey PRIMARY KEY (id);
ALTER TABLE ONLY product
    ADD CONSTRAINT product_pkey PRIMARY KEY (id);
ALTER TABLE ONLY product
    ADD CONSTRAINT product_product_code_key UNIQUE (product_code);
ALTER TABLE ONLY product_sku
    ADD CONSTRAINT product_sku_pkey PRIMARY KEY (id);
ALTER TABLE ONLY product_sku
    ADD CONSTRAINT product_sku_sku_code_key UNIQUE (sku_code);
ALTER TABLE ONLY product_spec
    ADD CONSTRAINT product_spec_pkey PRIMARY KEY (id);
ALTER TABLE ONLY refund
    ADD CONSTRAINT refund_pkey PRIMARY KEY (id);
ALTER TABLE ONLY roles
    ADD CONSTRAINT roles_name_key UNIQUE (name);
ALTER TABLE ONLY roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);
ALTER TABLE ONLY shipment
    ADD CONSTRAINT shipment_pkey PRIMARY KEY (id);
ALTER TABLE ONLY stock_reservation
    ADD CONSTRAINT stock_reservation_pkey PRIMARY KEY (id);
ALTER TABLE ONLY tracking_record
    ADD CONSTRAINT tracking_record_pkey PRIMARY KEY (id);
ALTER TABLE ONLY cart_item
    ADD CONSTRAINT uk_cart_sku UNIQUE (cart_id, sku_id);
ALTER TABLE ONLY inventory_reservation
    ADD CONSTRAINT uk_order_sku UNIQUE (order_no, sku_id);
ALTER TABLE ONLY processed_event
    ADD CONSTRAINT uk_processed_event UNIQUE (event_id, consumer_name);
ALTER TABLE ONLY users
    ADD CONSTRAINT users_email_key UNIQUE (email);
ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);
ALTER TABLE ONLY users
    ADD CONSTRAINT users_username_key UNIQUE (username);
ALTER TABLE ONLY warehouse
    ADD CONSTRAINT warehouse_code_key UNIQUE (code);
ALTER TABLE ONLY warehouse
    ADD CONSTRAINT warehouse_pkey PRIMARY KEY (id);
CREATE INDEX idx_audit_created ON product_audit_record USING btree (created_time);
CREATE INDEX idx_buyer_id ON orders USING btree (buyer_id);
CREATE INDEX idx_buyer_status_created ON orders USING btree (buyer_id, order_status, created_time);
CREATE INDEX idx_cart_user_id ON cart USING btree (user_id);
CREATE INDEX idx_category_id ON product USING btree (category_id);
CREATE INDEX idx_created_time ON fulfillment USING btree (created_time);
CREATE INDEX idx_merchant_id ON fulfillment USING btree (merchant_id);
CREATE INDEX idx_merchant_status ON product USING btree (merchant_id, status);
CREATE INDEX idx_merchant_status_created ON orders USING btree (merchant_id, order_status, created_time);
CREATE INDEX idx_movement_business_id ON inventory_movement USING btree (business_id);
CREATE INDEX idx_movement_created_time ON inventory_movement USING btree (created_time);
CREATE INDEX idx_movement_inventory_id ON inventory_movement USING btree (inventory_id);
CREATE INDEX idx_movement_reason_code ON inventory_movement USING btree (reason_code);
CREATE INDEX idx_movement_sku_created ON inventory_movement USING btree (product_sku_id, created_time);
CREATE INDEX idx_movement_sku_id ON inventory_movement USING btree (product_sku_id);
CREATE INDEX idx_movement_source_id ON inventory_movement USING btree (source_id);
CREATE INDEX idx_movement_source_type ON inventory_movement USING btree (source_type);
CREATE INDEX idx_movement_type ON inventory_movement USING btree (movement_type);
CREATE INDEX idx_order_no ON checkout_transaction USING btree (order_no);
CREATE INDEX idx_order_status ON orders USING btree (order_status);
CREATE INDEX idx_outbox_status ON outbox_event USING btree (status);
CREATE INDEX idx_pkt_fulfillment_id ON packing_task USING btree (fulfillment_id);
CREATE INDEX idx_pkt_picking_task_id ON packing_task USING btree (picking_task_id);
CREATE INDEX idx_pkt_status ON packing_task USING btree (status);
CREATE INDEX idx_product_cover ON product_image USING btree (product_id, is_cover);
CREATE INDEX idx_product_id ON inventory USING btree (product_id);
CREATE INDEX idx_pt_fulfillment_id ON picking_task USING btree (fulfillment_id);
CREATE INDEX idx_pt_status ON picking_task USING btree (status);
CREATE INDEX idx_pt_warehouse_id ON picking_task USING btree (warehouse_id);
CREATE INDEX idx_reservation_created_time ON inventory_reservation USING btree (created_time);
CREATE INDEX idx_reservation_inventory_id ON inventory_reservation USING btree (inventory_id);
CREATE INDEX idx_reservation_order_id ON inventory_reservation USING btree (order_id);
CREATE INDEX idx_reservation_sku_id ON inventory_reservation USING btree (product_sku_id);
CREATE INDEX idx_reservation_status_expired ON inventory_reservation USING btree (status, expire_time);
CREATE INDEX idx_ship_fulfillment_id ON shipment USING btree (fulfillment_id);
CREATE INDEX idx_ship_status ON shipment USING btree (status);
CREATE INDEX idx_ship_tracking_number ON shipment USING btree (tracking_number);
CREATE INDEX idx_sku_status ON product_sku USING btree (product_id, status);
CREATE INDEX idx_status ON fulfillment USING btree (status);
CREATE INDEX idx_status_created ON product USING btree (status, created_time);
CREATE INDEX idx_store_id ON product USING btree (store_id);
CREATE INDEX idx_tr_shipment_id ON tracking_record USING btree (shipment_id);
CREATE INDEX idx_user_id ON checkout_transaction USING btree (user_id);
CREATE INDEX idx_warehouse_status ON warehouse USING btree (status);
ALTER TABLE ONLY cart_item
    ADD CONSTRAINT fk1uobyhgl1wvgt1jpccia8xxs3 FOREIGN KEY (cart_id) REFERENCES cart(id);
ALTER TABLE ONLY product_image
    ADD CONSTRAINT fk6oo0cvcdtb6qmwsga468uuukk FOREIGN KEY (product_id) REFERENCES product(id);
ALTER TABLE ONLY product_spec
    ADD CONSTRAINT fke1voctn4pw0tw1sxq6gvaaant FOREIGN KEY (product_id) REFERENCES product(id);
ALTER TABLE ONLY product_sku
    ADD CONSTRAINT fklh9qu0pcf5622eexwh1lmc157 FOREIGN KEY (product_id) REFERENCES product(id);
-- PostgreSQL database dump complete
