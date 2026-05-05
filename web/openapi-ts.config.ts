import { defineConfig } from "@hey-api/openapi-ts";

export default defineConfig({
  input: "../open-api.json",
  output: "src/lib/api/generated",
  plugins: ["@hey-api/client-next", "@hey-api/typescript", "@hey-api/sdk"],
});
