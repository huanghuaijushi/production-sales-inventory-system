<template>
  <RouterView />
  <ConfirmDialog />
  <AppToast />
</template>

<script setup lang="ts">
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import AppToast from '@/components/common/AppToast.vue'
</script>
