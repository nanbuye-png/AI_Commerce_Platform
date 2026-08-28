const path = require('node:path');
const Module = require('node:module');

const customerNodeModules = path.resolve(__dirname, '../frontend/customer-web/node_modules');
process.env.NODE_PATH = [customerNodeModules, process.env.NODE_PATH]
  .filter(Boolean)
  .join(path.delimiter);
Module._initPaths();
