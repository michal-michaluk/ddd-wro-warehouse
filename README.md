# ddd-wro-warehouse
[![Licence MIT](http://img.shields.io/badge/license-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://travis-ci.org/michal-michaluk/ddd-wro-warehouse.svg?branch=master)](https://travis-ci.org/michal-michaluk/ddd-wro-warehouse)
[![Code Coverage](https://codecov.io/gh/michal-michaluk/ddd-wro-warehouse/branch/master/graph/badge.svg)](https://codecov.io/gh/michal-michaluk/ddd-wro-warehouse)

Warehouse model developed during http://www.meetup.com/DDD-WRO/ meetings.

A producer of plastic parts for automotive industry and household devices, controls their storage "manually" (with paper documents) and without managing the place where the things are stored. 

The storage supplies a production process with components and keeps the made products before sending them to customer.

A driver brings production components from supplier to the storage and takes products to the customer. The driver comes with the list of products to take. The customer prefers to get those products first which were ordered first (FIFO: first in - first out).

When a quality department discovers bad product, wants to remove the whole party of products made from the same production component (e.g. plastic granulate). 
