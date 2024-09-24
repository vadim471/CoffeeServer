ALTER TABLE coffee_order
ADD COLUMN id_product INTEGER,
ADD COLUMN product_price_summ INTEGER;

ALTER TABLE coffee_order
RENAME COLUMN product_amount TO product_last_price;