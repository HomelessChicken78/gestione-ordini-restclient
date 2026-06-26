package it.itsacademy.gestioneordinirestclient.service;

import it.itsacademy.gestioneordinirestclient.dto.PresignedUrlDTO;
import it.itsacademy.gestioneordinirestclient.model.Ordine;
import it.itsacademy.gestioneordinirestclient.repository.RepositoryOrdine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.List;

@Service @Transactional
@RequiredArgsConstructor @Slf4j
public class RicevutaServiceImpl implements RicevutaService {
    private final RepositoryOrdine repositoryOrdine;
    private final S3Presigner presigner;

    @Value("${s3.bucket.name}")
    private String bucketS3;

    @Value("${s3.bucket.prefixes.root}")
    private String rootPrefix;

    @Override
    public List<String> searchAllReceiptsOfUser(String authHeader) {
        return repositoryOrdine.findByUsernameClienteAndNomeRicevutaNotNull(authHeader)
                .stream()
                .map(Ordine::getNomeRicevuta)
                .toList();
    }

    @Override
    public PresignedUrlDTO createPresignedUrl(String username, String receiptFileName) {
        repositoryOrdine.findByRicevutaAndUsernameOrThrow(username, receiptFileName);

        var getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketS3)
                .key(rootPrefix + "/" + username + "/" + receiptFileName + ".pdf")
                .build();

        String presignedUrl = presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(120))
                        .getObjectRequest(getObjectRequest)
                        .build())
                .url()
                .toString();

        return new PresignedUrlDTO(presignedUrl);
    }
}
