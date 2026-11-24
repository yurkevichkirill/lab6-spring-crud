INSERT INTO orders (user_id, product, quantity, amount, status, created_at) VALUES
  ('1', 'Laptop Lenovo ThinkPad', 1, 2450.00, 'PAID',      CURRENT_TIMESTAMP),
  ('1', 'USB-C Hub',              2,   89.90, 'SHIPPED',   CURRENT_TIMESTAMP),
  ('1', 'Mechanical Keyboard',    1,  320.00, 'NEW',       CURRENT_TIMESTAMP),
  ('2', 'Monitor 27"',            1,  780.00, 'PAID',      CURRENT_TIMESTAMP),
  ('2', 'HDMI Cable',             3,   15.50, 'DELIVERED', CURRENT_TIMESTAMP),
  ('3', 'Headphones',             1,  199.99, 'CANCELLED', CURRENT_TIMESTAMP);
