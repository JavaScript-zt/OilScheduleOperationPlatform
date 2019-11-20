%�״�ͼ
data = csvread('100-100-NSGAII/Experiment/PF/oilschedule.pf');
data = data(61:160,:);
varNames = { 'energyCost', 'pipeMixingCost', 'tankMixingCost', 'numberOfChange', 'numberOfTankUsed'};

subplot(1,2,1);
glyphplot(data,'glyph','star','varLabels',{ 'energyCost', 'pipeMixingCost', 'tankMixingCost', 'numberOfChange', 'numberOfTankUsed'});
box on
axis off

% ���ݱ�׼��
ND = normlization(data, 2);

% ȷ���������
k = getk(ND,50);
k = 16;

% ����
colors =  lines(k);
label = init_methods(ND, k, 2);

[data,ind] = sortrows([data,label],6);
subplot(1,2,2);
h=glyphplot(data(:,1:5),'glyph','star','varLabels',{ 'energyCost', 'pipeMixingCost', 'tankMixingCost', 'numberOfChange', 'numberOfTankUsed'},...
    'ObsLabels',num2str(ind));
for i=1:k
    hf = h(data(:,6)==i,1);
    ha = h(data(:,6)==i,2);
    for j=1:length(hf)
        hf(j).Color=colors(i,:);
        ha(j).Color=colors(i,:);
    end
end
box on
axis off

set(gcf,'Position',[600,162,900,400]);%���û�ͼ��С��λ��


%% ȷ���������
function k=getk(data,K)
    [n,p]=size(data);
    for i=1:p
       minr=min(data(:,i));
       maxr=max(data(:,i));
       data(:,i)=(data(:,i)-minr)/(maxr-minr);%��һ��
    end
    D=zeros(K-1,2);T=0;
    for k=2:K
        T=T+1;
        [lable,c,sumd,d]=kmeans(data,k);
        %data��n*pԭʼ��������
        %lable��n*1��������������ǩ��
        %c��k*p������k���������ĵ�λ��
        %sumd��1*k������������е���������ĵ����֮��
        %d��n*k������ÿ������������ĵľ���
        %-----��ÿ������-----
        sort_num=zeros(k,1);%ÿ������
        for i=1:k
            for j=1:n
                if lable(j,1)==i
                    sort_num(i,1)=sort_num(i,1)+1;
                end
            end
        end
        %-----��ÿ������-----
        sort_ind=sumd./sort_num;%ÿ������ƽ������
        sort_ind_ave=mean(sort_ind);%����ƽ������
        %-----�����ƽ������-----
        h=nchoosek(k,2);A=zeros(h,2);t=0;sort_outd=zeros(h,1);
        for i=1:k-1
            for j=i+1:k
                t=t+1;
                A(t,1)=i;
                A(t,2)=j;
            end
        end
        for i=1:h
            for j=1:p
                sort_outd(i,1)=sort_outd(i,1)+(c(A(i,1),j)-c(A(i,2),j))^2;
            end
        end
        sort_outd_ave=mean(sort_outd);%���ƽ������
        %-----�����ƽ������-----
        D(T,1)=k;
        D(T,2)=sort_ind_ave/sort_outd_ave;
    end
    % plot(D(:,1),D(:,2));
end

%% ����Ԥ����
    % ���룺�ޱ�ǩ���ݣ���������ѡ�񷽷�
    % ����������ǩ
function label=init_methods(data, K, choose)
    if choose==1
        %�����ʼ�������ѡK����Ϊ�������ģ�����ŷ�Ͼ�����������㵽����࣬�����ݼ���ΪK�࣬���ÿ�����������ǩ
        [X_num, ~]=size(data);
        rand_array=randperm(X_num);    %����1~X_num֮���������������   
        para_miu=data(rand_array(1:K), :);  %�������ȡǰK��������X������ȡ��K����Ϊ��ʼ��������
        %ŷ�Ͼ��룬���㣨X-para_miu��^2=X^2+para_miu^2-2*X*para_miu'�������СΪX_num*K
        distant=repmat(sum(data.*data,2),1,K)+repmat(sum(para_miu.*para_miu,2)',X_num,1)-2*data*para_miu';
        %����distantÿ����Сֵ���ڵ��±�
        [~,label]=min(distant,[],2);
    elseif choose==2
        %��kmeans���г�ʼ�����࣬�����ݼ���ΪK�࣬���ÿ�����������ǩ
        label=kmeans(data, K);
    elseif choose==3
        %��FCM�㷨���г�ʼ��
        options=[NaN, NaN, NaN, 0];
        [~, responsivity]=fcm(data, K, options);   %��FCM�㷨��������Ⱦ���
        [~, label]=max(responsivity', [], 2);
    end
end

%% ���ݹ�һ��
function data = normlization(data, choose)
    if choose==0
        % ����һ��
        data = data;
    elseif choose==1
        % Z-score��һ��
        data = bsxfun(@minus, data, mean(data));
        data = bsxfun(@rdivide, data, std(data));
    elseif choose==2
        % ���-��С��һ������
        [data_num,~]=size(data);
        data=(data-ones(data_num,1)*min(data))./(ones(data_num,1)*(max(data)-min(data)));
    end
end