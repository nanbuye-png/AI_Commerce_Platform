-- AI Commerce Platform baseline schema (PostgreSQL 16)
-- Sanitized from a verified pg_dump: psql meta commands and search_path reset removed.
--
-- PostgreSQL database dump
--


-- Dumped from database version 16.14
-- Dumped by pg_dump version 16.14

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: cart; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cart (
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    user_id bigint NOT NULL
);


--
-- Name: cart_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cart_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cart_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cart_id_seq OWNED BY public.cart.id;


--
-- Name: cart_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cart_item (
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


--
-- Name: cart_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cart_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cart_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cart_item_id_seq OWNED BY public.cart_item.id;


--
-- Name: category; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.category (
    deleted boolean NOT NULL,
    level integer NOT NULL,
    sort integer NOT NULL,
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    parent_id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    category_name character varying(64) NOT NULL
);


--
-- Name: category_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.category_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.category_id_seq OWNED BY public.category.id;


--
-- Name: checkout_transaction; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.checkout_transaction (
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


--
-- Name: checkout_transaction_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.checkout_transaction_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: checkout_transaction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.checkout_transaction_id_seq OWNED BY public.checkout_transaction.id;


--
-- Name: fulfillment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fulfillment (
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


--
-- Name: fulfillment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.fulfillment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fulfillment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.fulfillment_id_seq OWNED BY public.fulfillment.id;


--
-- Name: inventory; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.inventory (
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


--
-- Name: inventory_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.inventory_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: inventory_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.inventory_id_seq OWNED BY public.inventory.id;


--
-- Name: inventory_movement; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.inventory_movement (
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


--
-- Name: inventory_movement_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.inventory_movement_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: inventory_movement_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.inventory_movement_id_seq OWNED BY public.inventory_movement.id;


--
-- Name: inventory_reservation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.inventory_reservation (
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


--
-- Name: inventory_reservation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.inventory_reservation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: inventory_reservation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.inventory_reservation_id_seq OWNED BY public.inventory_reservation.id;


--
-- Name: orders; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.orders (
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


--
-- Name: orders_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.orders_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: orders_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.orders_id_seq OWNED BY public.orders.id;


--
-- Name: outbox_event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.outbox_event (
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


--
-- Name: outbox_event_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.outbox_event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: outbox_event_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.outbox_event_id_seq OWNED BY public.outbox_event.id;


--
-- Name: packing_task; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.packing_task (
    created_time timestamp(6) without time zone,
    fulfillment_id bigint NOT NULL,
    id bigint NOT NULL,
    packed_at timestamp(6) without time zone,
    picking_task_id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    status character varying(20) NOT NULL,
    CONSTRAINT packing_task_status_check CHECK (((status)::text = ANY ((ARRAY['CREATED'::character varying, 'PROCESSING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: packing_task_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.packing_task_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: packing_task_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.packing_task_id_seq OWNED BY public.packing_task.id;


--
-- Name: payment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payment (
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


--
-- Name: payment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.payment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: payment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.payment_id_seq OWNED BY public.payment.id;


--
-- Name: permissions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.permissions (
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    code character varying(100) NOT NULL,
    name character varying(100) NOT NULL,
    description character varying(255)
);


--
-- Name: permissions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.permissions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: permissions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.permissions_id_seq OWNED BY public.permissions.id;


--
-- Name: picking_task; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.picking_task (
    completed_at timestamp(6) without time zone,
    created_time timestamp(6) without time zone,
    fulfillment_id bigint NOT NULL,
    id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    warehouse_id bigint NOT NULL,
    status character varying(20) NOT NULL,
    CONSTRAINT picking_task_status_check CHECK (((status)::text = ANY ((ARRAY['CREATED'::character varying, 'PROCESSING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: picking_task_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.picking_task_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: picking_task_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.picking_task_id_seq OWNED BY public.picking_task.id;


--
-- Name: processed_event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.processed_event (
    id bigint NOT NULL,
    processed_time timestamp(6) without time zone,
    event_id character varying(64) NOT NULL,
    consumer_name character varying(255) NOT NULL
);


--
-- Name: processed_event_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.processed_event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: processed_event_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.processed_event_id_seq OWNED BY public.processed_event.id;


--
-- Name: product; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product (
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


--
-- Name: product_audit_record; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_audit_record (
    created_time timestamp(6) without time zone NOT NULL,
    id bigint NOT NULL,
    product_id bigint NOT NULL,
    reviewer_id bigint NOT NULL,
    action character varying(20) NOT NULL,
    after_status character varying(20) NOT NULL,
    before_status character varying(20) NOT NULL,
    audit_remark character varying(500)
);


--
-- Name: product_audit_record_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.product_audit_record_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: product_audit_record_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.product_audit_record_id_seq OWNED BY public.product_audit_record.id;


--
-- Name: product_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.product_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: product_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.product_id_seq OWNED BY public.product.id;


--
-- Name: product_image; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_image (
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


--
-- Name: product_image_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.product_image_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: product_image_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.product_image_id_seq OWNED BY public.product_image.id;


--
-- Name: product_sku; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_sku (
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


--
-- Name: product_sku_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.product_sku_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: product_sku_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.product_sku_id_seq OWNED BY public.product_sku.id;


--
-- Name: product_spec; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_spec (
    deleted boolean NOT NULL,
    sort integer NOT NULL,
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    product_id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    spec_name character varying(64) NOT NULL,
    spec_values json NOT NULL
);


--
-- Name: product_spec_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.product_spec_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: product_spec_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.product_spec_id_seq OWNED BY public.product_spec.id;


--
-- Name: refund; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.refund (
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


--
-- Name: refund_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.refund_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: refund_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.refund_id_seq OWNED BY public.refund.id;


--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    name character varying(50) NOT NULL,
    description character varying(255)
);


--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.roles_id_seq OWNED BY public.roles.id;


--
-- Name: shipment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.shipment (
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


--
-- Name: shipment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.shipment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: shipment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.shipment_id_seq OWNED BY public.shipment.id;


--
-- Name: stock_reservation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stock_reservation (
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


--
-- Name: stock_reservation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.stock_reservation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: stock_reservation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.stock_reservation_id_seq OWNED BY public.stock_reservation.id;


--
-- Name: tracking_record; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tracking_record (
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    occurred_at timestamp(6) without time zone,
    shipment_id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    status character varying(50),
    location character varying(200),
    description character varying(500)
);


--
-- Name: tracking_record_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tracking_record_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tracking_record_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tracking_record_id_seq OWNED BY public.tracking_record.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
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


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: warehouse; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.warehouse (
    created_time timestamp(6) without time zone,
    id bigint NOT NULL,
    updated_time timestamp(6) without time zone,
    status character varying(20) NOT NULL,
    code character varying(50) NOT NULL,
    name character varying(200) NOT NULL,
    address character varying(500),
    CONSTRAINT warehouse_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'MAINTENANCE'::character varying])::text[])))
);


--
-- Name: warehouse_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.warehouse_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: warehouse_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.warehouse_id_seq OWNED BY public.warehouse.id;


--
-- Name: cart id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart ALTER COLUMN id SET DEFAULT nextval('public.cart_id_seq'::regclass);


--
-- Name: cart_item id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_item ALTER COLUMN id SET DEFAULT nextval('public.cart_item_id_seq'::regclass);


--
-- Name: category id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.category ALTER COLUMN id SET DEFAULT nextval('public.category_id_seq'::regclass);


--
-- Name: checkout_transaction id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkout_transaction ALTER COLUMN id SET DEFAULT nextval('public.checkout_transaction_id_seq'::regclass);


--
-- Name: fulfillment id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fulfillment ALTER COLUMN id SET DEFAULT nextval('public.fulfillment_id_seq'::regclass);


--
-- Name: inventory id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory ALTER COLUMN id SET DEFAULT nextval('public.inventory_id_seq'::regclass);


--
-- Name: inventory_movement id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory_movement ALTER COLUMN id SET DEFAULT nextval('public.inventory_movement_id_seq'::regclass);


--
-- Name: inventory_reservation id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory_reservation ALTER COLUMN id SET DEFAULT nextval('public.inventory_reservation_id_seq'::regclass);


--
-- Name: orders id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders ALTER COLUMN id SET DEFAULT nextval('public.orders_id_seq'::regclass);


--
-- Name: outbox_event id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.outbox_event ALTER COLUMN id SET DEFAULT nextval('public.outbox_event_id_seq'::regclass);


--
-- Name: packing_task id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.packing_task ALTER COLUMN id SET DEFAULT nextval('public.packing_task_id_seq'::regclass);


--
-- Name: payment id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment ALTER COLUMN id SET DEFAULT nextval('public.payment_id_seq'::regclass);


--
-- Name: permissions id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permissions ALTER COLUMN id SET DEFAULT nextval('public.permissions_id_seq'::regclass);


--
-- Name: picking_task id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.picking_task ALTER COLUMN id SET DEFAULT nextval('public.picking_task_id_seq'::regclass);


--
-- Name: processed_event id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.processed_event ALTER COLUMN id SET DEFAULT nextval('public.processed_event_id_seq'::regclass);


--
-- Name: product id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product ALTER COLUMN id SET DEFAULT nextval('public.product_id_seq'::regclass);


--
-- Name: product_audit_record id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_audit_record ALTER COLUMN id SET DEFAULT nextval('public.product_audit_record_id_seq'::regclass);


--
-- Name: product_image id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_image ALTER COLUMN id SET DEFAULT nextval('public.product_image_id_seq'::regclass);


--
-- Name: product_sku id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_sku ALTER COLUMN id SET DEFAULT nextval('public.product_sku_id_seq'::regclass);


--
-- Name: product_spec id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_spec ALTER COLUMN id SET DEFAULT nextval('public.product_spec_id_seq'::regclass);


--
-- Name: refund id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refund ALTER COLUMN id SET DEFAULT nextval('public.refund_id_seq'::regclass);


--
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.roles_id_seq'::regclass);


--
-- Name: shipment id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.shipment ALTER COLUMN id SET DEFAULT nextval('public.shipment_id_seq'::regclass);


--
-- Name: stock_reservation id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock_reservation ALTER COLUMN id SET DEFAULT nextval('public.stock_reservation_id_seq'::regclass);


--
-- Name: tracking_record id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tracking_record ALTER COLUMN id SET DEFAULT nextval('public.tracking_record_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: warehouse id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warehouse ALTER COLUMN id SET DEFAULT nextval('public.warehouse_id_seq'::regclass);


--
-- Name: cart_item cart_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_item
    ADD CONSTRAINT cart_item_pkey PRIMARY KEY (id);


--
-- Name: cart cart_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart
    ADD CONSTRAINT cart_pkey PRIMARY KEY (id);


--
-- Name: category category_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_pkey PRIMARY KEY (id);


--
-- Name: checkout_transaction checkout_transaction_checkout_no_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkout_transaction
    ADD CONSTRAINT checkout_transaction_checkout_no_key UNIQUE (checkout_no);


--
-- Name: checkout_transaction checkout_transaction_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkout_transaction
    ADD CONSTRAINT checkout_transaction_pkey PRIMARY KEY (id);


--
-- Name: fulfillment fulfillment_order_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fulfillment
    ADD CONSTRAINT fulfillment_order_id_key UNIQUE (order_id);


--
-- Name: fulfillment fulfillment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fulfillment
    ADD CONSTRAINT fulfillment_pkey PRIMARY KEY (id);


--
-- Name: inventory_movement inventory_movement_movement_no_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory_movement
    ADD CONSTRAINT inventory_movement_movement_no_key UNIQUE (movement_no);


--
-- Name: inventory_movement inventory_movement_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory_movement
    ADD CONSTRAINT inventory_movement_pkey PRIMARY KEY (id);


--
-- Name: inventory inventory_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory
    ADD CONSTRAINT inventory_pkey PRIMARY KEY (id);


--
-- Name: inventory_reservation inventory_reservation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory_reservation
    ADD CONSTRAINT inventory_reservation_pkey PRIMARY KEY (id);


--
-- Name: inventory_reservation inventory_reservation_reservation_no_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory_reservation
    ADD CONSTRAINT inventory_reservation_reservation_no_key UNIQUE (reservation_no);


--
-- Name: inventory inventory_sku_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory
    ADD CONSTRAINT inventory_sku_id_key UNIQUE (sku_id);


--
-- Name: orders orders_order_no_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_order_no_key UNIQUE (order_no);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- Name: outbox_event outbox_event_event_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.outbox_event
    ADD CONSTRAINT outbox_event_event_id_key UNIQUE (event_id);


--
-- Name: outbox_event outbox_event_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.outbox_event
    ADD CONSTRAINT outbox_event_pkey PRIMARY KEY (id);


--
-- Name: packing_task packing_task_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.packing_task
    ADD CONSTRAINT packing_task_pkey PRIMARY KEY (id);


--
-- Name: payment payment_payment_no_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment
    ADD CONSTRAINT payment_payment_no_key UNIQUE (payment_no);


--
-- Name: payment payment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment
    ADD CONSTRAINT payment_pkey PRIMARY KEY (id);


--
-- Name: permissions permissions_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_code_key UNIQUE (code);


--
-- Name: permissions permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_pkey PRIMARY KEY (id);


--
-- Name: picking_task picking_task_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.picking_task
    ADD CONSTRAINT picking_task_pkey PRIMARY KEY (id);


--
-- Name: processed_event processed_event_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.processed_event
    ADD CONSTRAINT processed_event_pkey PRIMARY KEY (id);


--
-- Name: product_audit_record product_audit_record_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_audit_record
    ADD CONSTRAINT product_audit_record_pkey PRIMARY KEY (id);


--
-- Name: product_image product_image_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_image
    ADD CONSTRAINT product_image_pkey PRIMARY KEY (id);


--
-- Name: product product_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT product_pkey PRIMARY KEY (id);


--
-- Name: product product_product_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT product_product_code_key UNIQUE (product_code);


--
-- Name: product_sku product_sku_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_sku
    ADD CONSTRAINT product_sku_pkey PRIMARY KEY (id);


--
-- Name: product_sku product_sku_sku_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_sku
    ADD CONSTRAINT product_sku_sku_code_key UNIQUE (sku_code);


--
-- Name: product_spec product_spec_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_spec
    ADD CONSTRAINT product_spec_pkey PRIMARY KEY (id);


--
-- Name: refund refund_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refund
    ADD CONSTRAINT refund_pkey PRIMARY KEY (id);


--
-- Name: roles roles_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_name_key UNIQUE (name);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: shipment shipment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.shipment
    ADD CONSTRAINT shipment_pkey PRIMARY KEY (id);


--
-- Name: stock_reservation stock_reservation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock_reservation
    ADD CONSTRAINT stock_reservation_pkey PRIMARY KEY (id);


--
-- Name: tracking_record tracking_record_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tracking_record
    ADD CONSTRAINT tracking_record_pkey PRIMARY KEY (id);


--
-- Name: cart_item uk_cart_sku; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_item
    ADD CONSTRAINT uk_cart_sku UNIQUE (cart_id, sku_id);


--
-- Name: inventory_reservation uk_order_sku; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory_reservation
    ADD CONSTRAINT uk_order_sku UNIQUE (order_no, sku_id);


--
-- Name: processed_event uk_processed_event; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.processed_event
    ADD CONSTRAINT uk_processed_event UNIQUE (event_id, consumer_name);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: warehouse warehouse_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warehouse
    ADD CONSTRAINT warehouse_code_key UNIQUE (code);


--
-- Name: warehouse warehouse_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warehouse
    ADD CONSTRAINT warehouse_pkey PRIMARY KEY (id);


--
-- Name: idx_audit_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_created ON public.product_audit_record USING btree (created_time);


--
-- Name: idx_buyer_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buyer_id ON public.orders USING btree (buyer_id);


--
-- Name: idx_buyer_status_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buyer_status_created ON public.orders USING btree (buyer_id, order_status, created_time);


--
-- Name: idx_cart_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cart_user_id ON public.cart USING btree (user_id);


--
-- Name: idx_category_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_category_id ON public.product USING btree (category_id);


--
-- Name: idx_created_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_created_time ON public.fulfillment USING btree (created_time);


--
-- Name: idx_merchant_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_merchant_id ON public.fulfillment USING btree (merchant_id);


--
-- Name: idx_merchant_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_merchant_status ON public.product USING btree (merchant_id, status);


--
-- Name: idx_merchant_status_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_merchant_status_created ON public.orders USING btree (merchant_id, order_status, created_time);


--
-- Name: idx_movement_business_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_movement_business_id ON public.inventory_movement USING btree (business_id);


--
-- Name: idx_movement_created_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_movement_created_time ON public.inventory_movement USING btree (created_time);


--
-- Name: idx_movement_inventory_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_movement_inventory_id ON public.inventory_movement USING btree (inventory_id);


--
-- Name: idx_movement_reason_code; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_movement_reason_code ON public.inventory_movement USING btree (reason_code);


--
-- Name: idx_movement_sku_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_movement_sku_created ON public.inventory_movement USING btree (product_sku_id, created_time);


--
-- Name: idx_movement_sku_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_movement_sku_id ON public.inventory_movement USING btree (product_sku_id);


--
-- Name: idx_movement_source_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_movement_source_id ON public.inventory_movement USING btree (source_id);


--
-- Name: idx_movement_source_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_movement_source_type ON public.inventory_movement USING btree (source_type);


--
-- Name: idx_movement_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_movement_type ON public.inventory_movement USING btree (movement_type);


--
-- Name: idx_order_no; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_order_no ON public.checkout_transaction USING btree (order_no);


--
-- Name: idx_order_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_order_status ON public.orders USING btree (order_status);


--
-- Name: idx_outbox_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_outbox_status ON public.outbox_event USING btree (status);


--
-- Name: idx_pkt_fulfillment_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pkt_fulfillment_id ON public.packing_task USING btree (fulfillment_id);


--
-- Name: idx_pkt_picking_task_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pkt_picking_task_id ON public.packing_task USING btree (picking_task_id);


--
-- Name: idx_pkt_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pkt_status ON public.packing_task USING btree (status);


--
-- Name: idx_product_cover; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_product_cover ON public.product_image USING btree (product_id, is_cover);


--
-- Name: idx_product_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_product_id ON public.inventory USING btree (product_id);


--
-- Name: idx_pt_fulfillment_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pt_fulfillment_id ON public.picking_task USING btree (fulfillment_id);


--
-- Name: idx_pt_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pt_status ON public.picking_task USING btree (status);


--
-- Name: idx_pt_warehouse_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pt_warehouse_id ON public.picking_task USING btree (warehouse_id);


--
-- Name: idx_reservation_created_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reservation_created_time ON public.inventory_reservation USING btree (created_time);


--
-- Name: idx_reservation_inventory_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reservation_inventory_id ON public.inventory_reservation USING btree (inventory_id);


--
-- Name: idx_reservation_order_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reservation_order_id ON public.inventory_reservation USING btree (order_id);


--
-- Name: idx_reservation_sku_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reservation_sku_id ON public.inventory_reservation USING btree (product_sku_id);


--
-- Name: idx_reservation_status_expired; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reservation_status_expired ON public.inventory_reservation USING btree (status, expire_time);


--
-- Name: idx_ship_fulfillment_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ship_fulfillment_id ON public.shipment USING btree (fulfillment_id);


--
-- Name: idx_ship_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ship_status ON public.shipment USING btree (status);


--
-- Name: idx_ship_tracking_number; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ship_tracking_number ON public.shipment USING btree (tracking_number);


--
-- Name: idx_sku_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sku_status ON public.product_sku USING btree (product_id, status);


--
-- Name: idx_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_status ON public.fulfillment USING btree (status);


--
-- Name: idx_status_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_status_created ON public.product USING btree (status, created_time);


--
-- Name: idx_store_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_store_id ON public.product USING btree (store_id);


--
-- Name: idx_tr_shipment_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tr_shipment_id ON public.tracking_record USING btree (shipment_id);


--
-- Name: idx_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_id ON public.checkout_transaction USING btree (user_id);


--
-- Name: idx_warehouse_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_warehouse_status ON public.warehouse USING btree (status);


--
-- Name: cart_item fk1uobyhgl1wvgt1jpccia8xxs3; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_item
    ADD CONSTRAINT fk1uobyhgl1wvgt1jpccia8xxs3 FOREIGN KEY (cart_id) REFERENCES public.cart(id);


--
-- Name: product_image fk6oo0cvcdtb6qmwsga468uuukk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_image
    ADD CONSTRAINT fk6oo0cvcdtb6qmwsga468uuukk FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- Name: product_spec fke1voctn4pw0tw1sxq6gvaaant; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_spec
    ADD CONSTRAINT fke1voctn4pw0tw1sxq6gvaaant FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- Name: product_sku fklh9qu0pcf5622eexwh1lmc157; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_sku
    ADD CONSTRAINT fklh9qu0pcf5622eexwh1lmc157 FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- PostgreSQL database dump complete
--
-- End of baseline schema.
