package com.jyf9774.bookyi;


import android.content.Context;
import android.net.Uri;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.object.DeleteObjectRequest;
import com.tencent.cos.xml.model.object.DeleteObjectResult;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.cos.xml.transfer.TransferState;
import com.tencent.cos.xml.transfer.TransferStateListener;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;

import java.util.HashSet;
import java.util.Set;

public class TencentCloudCos {

    String region = "ap-chengdu";
    String secretId = "AKIDe0tZ6O1M1Q6B1ctfkEeLivupUEl9wAXf"; //永久密钥 secretId
    String secretKey = "5uqg9UVK0RflbcPzZ1Vt4zTlurcdQ0KU"; //永久密钥 secretKey
    CosXmlService cosXmlService;

    TransferConfig transferConfig;
    TransferManager transferManager;
    String bucket = "jyf-1256919232";
    COSXMLUploadTask cosxmlUploadTask;

    public void init(Context context) {
        // 创建 CosXmlServiceConfig 对象，根据需要修改默认的配置参数
        CosXmlServiceConfig serviceConfig = new CosXmlServiceConfig.Builder()
                .setRegion(region)
                .isHttps(true) // 使用 HTTPS 请求, 默认为 HTTP 请求
                .builder();
        /**
         * 初始化 {@link QCloudCredentialProvider} 对象，来给 SDK 提供临时密钥
         * @parma secretId 永久密钥 secretId
         * @param secretKey 永久密钥 secretKey
         * @param keyDuration 密钥有效期，单位为秒
         */
        QCloudCredentialProvider credentialProvider = new ShortTimeCredentialProvider(secretId, secretKey, 300);
        cosXmlService = new CosXmlService(context, serviceConfig, credentialProvider);
    }

    public void uploadFile(String path, String filename) {
        // 初始化 TransferConfig
        transferConfig = new TransferConfig.Builder().build();
        /*若有特殊要求，则可以如下进行初始化定制。例如限定当对象 >= 2M 时，启用分块上传，且分块上传的分块大小为1M，当源对象大于5M时启用分块复制，且分块复制的大小为5M。*/
        transferConfig = new TransferConfig.Builder()
                .setDividsionForCopy(5 * 1024 * 1024) // 是否启用分块复制的最小对象大小
                .setSliceSizeForCopy(5 * 1024 * 1024) // 分块复制时的分块大小
                .setDivisionForUpload(2 * 1024 * 1024) // 是否启用分块上传的最小对象大小
                .setSliceSizeForUpload(1024 * 1024) // 分块上传时的分块大小
                .build();

// 初始化 TransferManager
        transferManager = new TransferManager(cosXmlService, transferConfig);

        String uploadId = null; //若存在初始化分块上传的 UploadId，则赋值对应的 uploadId 值用于续传；否则，赋值 null
// 上传对象
        cosxmlUploadTask = transferManager.upload(bucket, filename, path, uploadId);
    }

    public String deleteFile(String filename) {
        DeleteObjectRequest newRequest = new DeleteObjectRequest(bucket, filename);
        try {
            DeleteObjectResult deleteObjectResult = cosXmlService.deleteObject(newRequest);
            if (deleteObjectResult.httpCode >= 200 && deleteObjectResult.httpCode < 300) {
                return"删除成功";
            } else {
                return"图片删除失败";

            }
        } catch (CosXmlClientException e) {
            e.printStackTrace();
            return "删除失败，ClientException";
        } catch (CosXmlServiceException e) {
            e.printStackTrace();
            return "删除失败，ServiceException";
        }


    }


}
