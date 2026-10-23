FROM node:20-alpine AS builder
WORKDIR /app

COPY package.json pnpm-lock.yaml* .npmrc ./
# corepack 下载 pnpm 本体默认走 registry.npmjs.org（国内不通），指到 npmmirror
ENV COREPACK_NPM_REGISTRY=https://registry.npmmirror.com
RUN corepack enable && pnpm install --frozen-lockfile

COPY . .
ARG VITE_APP_BASE_API=http://localhost:8080
ENV VITE_APP_BASE_API=$VITE_APP_BASE_API
RUN pnpm build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
