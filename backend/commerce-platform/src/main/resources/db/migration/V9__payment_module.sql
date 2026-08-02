-- ============================================================
-- V9: 支付模块 —— 商户二维码收款（Merchant QR Payment）
-- 1. orders 表新增商家接单时间、支付发起时间字段
-- 2. 新建 merchant_qr_payment 商户收款流水表
-- ============================================================

-- 商家接单时间
ALTER TABLE public.orders ADD COLUMN accept_time timestamp(6) without time zone;

-- 商家发起支付（生成商户二维码）时间
ALTER TABLE public.orders ADD COLUMN payment_init_time timestamp(6) without time zone;

-- 商户二维码收款流水表
CREATE TABLE public.merchant_qr_payment (
    id bigint NOT NULL,
    payment_no character varying(32) NOT NULL,
    order_no character varying(32) NOT NULL,
    buyer_id bigint NOT NULL,
    merchant_id bigint NOT NULL,
    amount numeric(10,2) NOT NULL,
    qr_token character varying(64) NOT NULL,
    status character varying(20) NOT NULL,
    expire_time timestamp(6) without time zone NOT NULL,
    paid_time timestamp(6) without time zone,
    cancelled_time timestamp(6) without time zone,
    created_time timestamp(6) without time zone,
    updated_time timestamp(6) without time zone,
    version bigint NOT NULL,
    deleted boolean NOT NULL,
    CONSTRAINT merchant_qr_payment_status_check CHECK (((status)::text = ANY ((ARRAY['WAITING'::character varying, 'PAID'::character varying, 'CANCELLED'::character varying, 'EXPIRED'::character varying])::text[])))
);

CREATE SEQUENCE public.merchant_qr_payment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.merchant_qr_payment_id_seq OWNED BY public.merchant_qr_payment.id;

ALTER TABLE ONLY public.merchant_qr_payment ALTER COLUMN id SET DEFAULT nextval('public.merchant_qr_payment_id_seq'::regclass);

ALTER TABLE ONLY public.merchant_qr_payment
    ADD CONSTRAINT merchant_qr_payment_pkey PRIMARY KEY (id);

CREATE INDEX idx_mqp_order_no ON public.merchant_qr_payment USING btree (order_no);
CREATE INDEX idx_mqp_qr_token ON public.merchant_qr_payment USING btree (qr_token);
CREATE INDEX idx_mqp_status_expire ON public.merchant_qr_payment USING btree (status, expire_time);