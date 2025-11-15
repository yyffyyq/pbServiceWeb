<template>
    <div>
        <h2>测试代码</h2>
        <button @click="handleTest">测试</button>
    </div>
</template>

<script setup >
import { getTest } from "@/api/index";
const handleTest =async () => {
    const response =await getTest()
    console.log(response)
}

</script>

<style lang="scss" scoped>

</style>